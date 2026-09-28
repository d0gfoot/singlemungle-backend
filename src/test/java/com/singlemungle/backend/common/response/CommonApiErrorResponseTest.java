package com.singlemungle.backend.common.response;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.user.entity.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommonApiErrorResponseTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Test
	void protectedApiWithoutTokenReturnsCommonUnauthorizedResponse() throws Exception {
		mockMvc.perform(get("/api/v1/pets"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_TOKEN"))
			.andExpect(jsonPath("$.message").isNotEmpty())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void invalidTokenReturnsCommonUnauthorizedResponse() throws Exception {
		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
	}

	@Test
	void unknownApiReturnsCommonNotFoundResponse() throws Exception {
		mockMvc.perform(get("/api/v1/not-existing")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("API_NOT_FOUND"));
	}

	@Test
	void unsupportedMethodReturnsCommonMethodNotAllowedResponse() throws Exception {
		mockMvc.perform(put("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	void unsupportedContentTypeReturnsCommonResponse() throws Exception {
		mockMvc.perform(post("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.TEXT_PLAIN)
				.content("pet"))
			.andExpect(status().isUnsupportedMediaType())
			.andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
	}

	@Test
	void malformedJsonReturnsCommonBadRequestResponse() throws Exception {
		mockMvc.perform(post("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{invalid-json"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void invalidPathVariableReturnsCommonBadRequestResponse() throws Exception {
		mockMvc.perform(delete("/api/v1/pets/not-a-number")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void missingMultipartImageReturnsCommonBadRequestResponse() throws Exception {
		mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.param("petId", "1")
				.param("additionalRequest", "깔끔하게")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void missingImageReturnsCommonBusinessErrorResponse() throws Exception {
		mockMvc.perform(get("/api/v1/beauty/style-simulations/images/missing.jpg")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("STYLE_SIMULATION_NOT_FOUND"));
	}

	private String bearerToken() {
		return "Bearer " + jwtTokenService.createAccessToken(1L, UserRole.USER);
	}
}
