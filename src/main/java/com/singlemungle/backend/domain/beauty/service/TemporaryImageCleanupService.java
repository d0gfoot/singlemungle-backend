package com.singlemungle.backend.domain.beauty.service;

import java.time.LocalDateTime;

import com.singlemungle.backend.domain.beauty.storage.TemporaryImageStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.image-storage.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class TemporaryImageCleanupService {

	private final TemporaryImageStorage temporaryImageStorage;

	@Value("${app.image-storage.cleanup.temp-retention-hours:24}")
	private long retentionHours;

	@Scheduled(cron = "${app.image-storage.cleanup.temp-cron:0 30 * * * *}")
	public int cleanupTemporaryImages() {
		int count = temporaryImageStorage.deleteOlderThan(LocalDateTime.now().minusHours(retentionHours));
		if (count > 0) {
			log.info("Cleaned up temporary style simulation images count={}", count);
		}
		return count;
	}
}
