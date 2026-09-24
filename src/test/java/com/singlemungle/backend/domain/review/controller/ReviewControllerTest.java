package com.singlemungle.backend.domain.review.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.user.entity.UserRole;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {
	"/db/testdata/cleanup-beauty-test-data.sql",
	"/db/testdata/beauty-test-data.sql"
})
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Test
	void createAndReadReviewWithAiReferenceConsent() throws Exception {
		MvcResult created = createReview(true, bearerToken(1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.googlePlaceId").value("ChIJ-demo-beauty-shop"))
			.andExpect(jsonPath("$.data.images[0].aiReferenceAllowed").value(true))
			.andReturn();
		Integer reviewId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.reviewId");

		mockMvc.perform(get("/api/v1/beauty-shops/ChIJ-demo-beauty-shop/reviews/{reviewId}", reviewId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.groomingStyleName").value("물개컷"));
 	}

	@Test
	void updateAndDeleteRequireReviewOwner() throws Exception {
		MvcResult created = createReview(false, bearerToken(1L)).andReturn();
		Integer reviewId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.reviewId");

		mockMvc.perform(patch("/api/v1/beauty-shops/ChIJ-demo-beauty-shop/reviews/{reviewId}", reviewId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken(2L))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"rating":4,"content":"수정","groomingStyleName":"물개컷","aiReferenceAllowed":true}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("REVIEW_NOT_FOUND"));

		mockMvc.perform(delete("/api/v1/beauty-shops/ChIJ-demo-beauty-shop/reviews/{reviewId}", reviewId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/beauty-shops/ChIJ-demo-beauty-shop/reviews/{reviewId}", reviewId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isNotFound());
	}

	@Test
	void simulationUsesOnlyConsentedReviewImageAndInfersShop() throws Exception {
		MvcResult consentedReview = createReview(true, bearerToken(1L)).andReturn();
		Integer reviewImageId = JsonPath.read(
			consentedReview.getResponse().getContentAsString(), "$.data.images[0].reviewImageId"
		);

		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "101")
				.param("referenceReviewImageId", reviewImageId.toString())
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.data.googlePlaceId").value("ChIJ-demo-beauty-shop"))
			.andExpect(jsonPath("$.data.referenceReviewImageId").value(reviewImageId))
			.andExpect(jsonPath("$.data.sourceType").value("REVIEW_IMAGE"));

		MvcResult deniedReview = createReview(false, bearerToken(1L)).andReturn();
		Integer deniedImageId = JsonPath.read(
			deniedReview.getResponse().getContentAsString(), "$.data.images[0].reviewImageId"
		);
		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "101")
				.param("referenceReviewImageId", deniedImageId.toString())
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("AI_REFERENCE_NOT_ALLOWED"));
	}

	private org.springframework.test.web.servlet.ResultActions createReview(
		boolean aiReferenceAllowed,
		String bearerToken
	) throws Exception {
		MockMultipartFile image = new MockMultipartFile(
			"images", "result.png", "image/png", new byte[] {1, 2, 3}
		);
		return mockMvc.perform(multipart("/api/v1/beauty-shops/ChIJ-demo-beauty-shop/reviews")
			.file(image)
			.param("rating", "5")
			.param("content", "미용 결과가 만족스러워요")
			.param("groomingStyleName", "물개컷")
			.param("aiReferenceAllowed", Boolean.toString(aiReferenceAllowed))
			.header(HttpHeaders.AUTHORIZATION, bearerToken));
	}

	private MockMultipartFile petImage() {
		return new MockMultipartFile("petImage", "pet.png", "image/png", new byte[] {4, 5, 6});
	}

	private String bearerToken(Long userId) {
		return "Bearer " + jwtTokenService.createAccessToken(userId, UserRole.USER);
	}
}
