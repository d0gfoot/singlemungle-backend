package com.singlemungle.backend.domain.beauty.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationClient;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
class BeautyStyleSimulationFailureIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Autowired
	private BeautyStyleSimulationRepository simulationRepository;

	@MockitoBean
	private ImageGenerationClient imageGenerationClient;

	@Test
	void generationFailurePersistsFailedStatusAndDeletesTemporarySourceImage() throws Exception {
		when(imageGenerationClient.generate(any(ImageGenerationRequest.class)))
			.thenThrow(new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED));

		MvcResult result = mockMvc.perform(multipart("/api/v1/beauty/style-simulations")
				.file(new MockMultipartFile("petImage", "pet.png", "image/png", new byte[] {1, 2, 3}))
				.param("petId", "101")
				.param("shopId", "301")
				.param("stylePresetId", "1")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.data.status").value("PENDING"))
			.andReturn();

		Integer simulationId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.simulationId");
		await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> {
				BeautyStyleSimulation simulation = simulationRepository.findById(simulationId.longValue()).orElseThrow();
				assertThat(simulation.getStatus()).isEqualTo(BeautyStyleSimulationStatus.FAILED);
				assertThat(simulation.getFailureReason()).isEqualTo(ErrorCode.IMAGE_GENERATION_FAILED.getCode());
				assertThat(simulation.getSourceImageUrl()).isNull();
			});
	}

	private String bearerToken() {
		return "Bearer " + jwtTokenService.createAccessToken(1L, UserRole.USER);
	}
}
