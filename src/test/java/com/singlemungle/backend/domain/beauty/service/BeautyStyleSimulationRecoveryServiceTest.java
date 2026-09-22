package com.singlemungle.backend.domain.beauty.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulationStatus;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(properties = {
	"app.image-storage.cleanup.enabled=true",
	"app.image-storage.cleanup.pending-cron=0 0 0 1 1 *",
	"app.image-storage.cleanup.pending-timeout-minutes=10"
})
@ActiveProfiles("test")
@Sql(scripts = {
	"/db/testdata/cleanup-beauty-test-data.sql",
	"/db/testdata/beauty-test-data.sql",
	"/db/testdata/beauty-stale-pending-test-data.sql"
})
class BeautyStyleSimulationRecoveryServiceTest {

	private static final Path STORAGE_DIRECTORY = Path.of("build/test-generated-images");

	@Autowired
	private BeautyStyleSimulationRecoveryService recoveryService;

	@Autowired
	private BeautyStyleSimulationRepository simulationRepository;

	@Test
	void failStalePendingSimulationsDeletesFilesAndPersistsFailedStatus() throws Exception {
		Path sourceImage = STORAGE_DIRECTORY.resolve("source-pending.png");
		Path styleImage = STORAGE_DIRECTORY.resolve("style-pending.png");
		Files.createDirectories(STORAGE_DIRECTORY);
		Files.write(sourceImage, new byte[] {1});
		Files.write(styleImage, new byte[] {2});

		int failedCount = recoveryService.failStalePendingSimulations();

		BeautyStyleSimulation simulation = simulationRepository.findById(903L).orElseThrow();
		assertThat(failedCount).isEqualTo(1);
		assertThat(simulation.getStatus()).isEqualTo(BeautyStyleSimulationStatus.FAILED);
		assertThat(simulation.getFailureReason()).isEqualTo(ErrorCode.IMAGE_GENERATION_FAILED.getCode());
		assertThat(simulation.getSourceImageUrl()).isNull();
		assertThat(simulation.getStyleImageUrl()).isNull();
		assertThat(sourceImage).doesNotExist();
		assertThat(styleImage).doesNotExist();
	}
}
