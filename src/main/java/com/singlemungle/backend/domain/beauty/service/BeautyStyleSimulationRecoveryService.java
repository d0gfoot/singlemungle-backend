package com.singlemungle.backend.domain.beauty.service;

import java.time.LocalDateTime;
import java.util.List;

import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulationStatus;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.image-storage.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class BeautyStyleSimulationRecoveryService {

	private final BeautyStyleSimulationRepository simulationRepository;
	private final ImageStorage imageStorage;

	@Value("${app.image-storage.cleanup.pending-timeout-minutes:10}")
	private long pendingTimeoutMinutes;

	@Scheduled(cron = "${app.image-storage.cleanup.pending-cron:0 * * * * *}")
	@Transactional
	public int failStalePendingSimulations() {
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(pendingTimeoutMinutes);
		List<BeautyStyleSimulation> staleSimulations = simulationRepository.findByStatusAndCreatedAtBefore(
			BeautyStyleSimulationStatus.PENDING,
			threshold
		);

		staleSimulations.forEach(simulation -> {
			imageStorage.delete(simulation.getSourceImageUrl());
			imageStorage.delete(simulation.getStyleImageUrl());
			simulation.fail(ErrorCode.IMAGE_GENERATION_FAILED.getCode());
		});

		if (!staleSimulations.isEmpty()) {
			log.warn("Marked stale beauty style simulations as failed count={}", staleSimulations.size());
		}

		return staleSimulations.size();
	}
}
