package com.singlemungle.backend.domain.beauty.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(properties = "app.image-storage.cleanup.enabled=true")
@ActiveProfiles("test")
@Sql(scripts = {
	"/db/testdata/cleanup-beauty-test-data.sql",
	"/db/testdata/beauty-test-data.sql",
	"/db/testdata/beauty-expired-simulation-test-data.sql"
})
class BeautyStyleSimulationCleanupServiceTest {

	private static final Path STORAGE_DIRECTORY = Path.of("build/test-generated-images");

	@Autowired
	private BeautyStyleSimulationCleanupService cleanupService;

	@Autowired
	private BeautyStyleSimulationRepository simulationRepository;

	@Test
	void cleanupExpiredSimulationsSoftDeletesExpiredRowsAndDeletesFiles() throws Exception {
		Path expiredSourceImage = STORAGE_DIRECTORY.resolve("source-expired.png");
		Path expiredGeneratedImage = STORAGE_DIRECTORY.resolve("generated-expired.png");
		Path activeGeneratedImage = STORAGE_DIRECTORY.resolve("generated-active.png");
		Files.createDirectories(STORAGE_DIRECTORY);
		Files.write(expiredSourceImage, new byte[] {1});
		Files.write(expiredGeneratedImage, new byte[] {2});
		Files.write(activeGeneratedImage, new byte[] {3});

		int cleanedCount = cleanupService.cleanupExpiredSimulations();

		assertThat(cleanedCount).isEqualTo(1);
		assertThat(Files.exists(expiredSourceImage)).isFalse();
		assertThat(Files.exists(expiredGeneratedImage)).isFalse();
		assertThat(Files.exists(activeGeneratedImage)).isTrue();
		assertThat(simulationRepository.findByExpiresAtBefore(LocalDateTime.now())).isEmpty();
		assertThat(simulationRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
			1L,
			LocalDateTime.now(),
			PageRequest.of(0, 10)
		).getContent()).hasSize(1);
	}
}
