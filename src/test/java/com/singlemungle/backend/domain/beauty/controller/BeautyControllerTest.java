package com.singlemungle.backend.domain.beauty.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulationStatus;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;
import com.singlemungle.backend.domain.user.entity.UserRole;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
class BeautyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Autowired
	private BeautyStyleSimulationRepository simulationRepository;

	@Test
	void getBeautyStylesReturnsShopStyles() throws Exception {
		mockMvc.perform(get("/api/v1/beauty-shops/{shopId}/styles", 301)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].styleId").value(401))
			.andExpect(jsonPath("$.data[0].styleName").value("곰돌이컷"));
	}

	@Test
	void getGroomingStylePresetsReturnsEnabledGlobalPresets() throws Exception {
		mockMvc.perform(get("/api/v1/beauty/style-presets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(5))
			.andExpect(jsonPath("$.data[0].name").value("곰돌이컷"));
	}

	@Test
	void createStyleSimulationReturnsPendingAndCompletesAsynchronously() throws Exception {
		MvcResult result = mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "101")
				.param("shopId", "301")
				.param("stylePresetId", "1")
				.param("additionalRequest", "좀 더 샤프하고 날렵하게")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.petId").value(101))
			.andExpect(jsonPath("$.data.shopId").value(301))
			.andExpect(jsonPath("$.data.styleKeyword").value("곰돌이컷"))
			.andExpect(jsonPath("$.data.stylePresetId").value(1))
			.andExpect(jsonPath("$.data.sourceType").value("PRESET"))
			.andExpect(jsonPath("$.data.additionalRequest").value("좀 더 샤프하고 날렵하게"))
			.andExpect(jsonPath("$.data.status").value("PENDING"))
			.andExpect(jsonPath("$.data.sourceImageUrl").doesNotExist())
			.andExpect(jsonPath("$.data.generatedImageUrl").doesNotExist())
			.andReturn();

		Integer simulationId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.simulationId");
		awaitCompleted(simulationId.longValue());
	}

	@Test
	void createStyleSimulationRequiresOwnedPet() throws Exception {
		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "102")
				.param("shopId", "301")
				.param("stylePresetId", "1")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void createStyleSimulationRequiresBeautyShop() throws Exception {
		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "101")
				.param("shopId", "302")
				.param("stylePresetId", "1")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PLACE_NOT_FOUND"));
	}

	@Test
	void createStyleSimulationValidatesImageContentType() throws Exception {
		MockMultipartFile invalidImage = new MockMultipartFile(
			"petImage",
			"pet.txt",
			"text/plain",
			"not-image".getBytes()
		);

		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(invalidImage)
				.param("petId", "101")
				.param("shopId", "301")
				.param("stylePresetId", "1")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void createStyleSimulationValidatesAdditionalRequestLength() throws Exception {
		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "101")
				.param("shopId", "301")
				.param("stylePresetId", "1")
				.param("additionalRequest", "a".repeat(501))
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void getStyleSimulationDetailRequiresOwner() throws Exception {
		mockMvc.perform(get("/api/v1/beauty/style-simulations/{simulationId}", 999)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("STYLE_SIMULATION_NOT_FOUND"));
	}

	@Test
	void getStyleSimulationsReturnsMyResults() throws Exception {
		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "101")
				.param("shopId", "301")
				.param("stylePresetId", "1")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.data.status").value("PENDING"))
			.andDo(result -> {
				Integer simulationId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.simulationId");
				awaitCompleted(simulationId.longValue());
			});

		mockMvc.perform(get("/api/v1/beauty/style-simulations")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.content[0].status").value("COMPLETED"));
	}

	@Test
	void deleteStyleSimulationSoftDeletesResult() throws Exception {
		MvcResult result = mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(petImage())
				.param("petId", "101")
				.param("shopId", "301")
				.param("stylePresetId", "1")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isAccepted())
			.andReturn();

		Integer simulationId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.simulationId");
		awaitCompleted(simulationId.longValue());
		BeautyStyleSimulation simulation = simulationRepository.findById(simulationId.longValue()).orElseThrow();
		Path generatedImage = storedImagePath(simulation.getGeneratedImageUrl());
		assertThat(simulation.getSourceImageUrl()).isNull();
		assertThat(generatedImage).exists();

		mockMvc.perform(delete("/api/v1/beauty/style-simulations/{simulationId}", simulationId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/beauty/style-simulations/{simulationId}", simulationId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("STYLE_SIMULATION_NOT_FOUND"));
		assertThat(generatedImage).doesNotExist();
	}

	private MockMultipartFile petImage() {
		return new MockMultipartFile(
			"petImage",
			"pet.png",
			"image/png",
			new byte[] {1, 2, 3}
		);
	}

	private void awaitCompleted(Long simulationId) {
		await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> {
				BeautyStyleSimulation simulation = simulationRepository.findById(simulationId).orElseThrow();
				assertThat(simulation.getStatus()).isEqualTo(BeautyStyleSimulationStatus.COMPLETED);
				assertThat(simulation.getGeneratedImageUrl()).isNotBlank();
			});
	}

	private Path storedImagePath(String imageUrl) {
		String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
		return Path.of("build/test-generated-images").resolve(fileName);
	}

	private String bearerToken() {
		return "Bearer " + jwtTokenService.createAccessToken(1L, UserRole.USER);
	}
}
