package com.singlemungle.backend.domain.beauty.service;

import java.time.LocalDateTime;
import java.util.List;

import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.image-storage.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class BeautyStyleSimulationCleanupService {

	private final BeautyStyleSimulationRepository simulationRepository;
	private final ImageStorage imageStorage;

	@Scheduled(cron = "${app.image-storage.cleanup.cron}")
	@Transactional
	public int cleanupExpiredSimulations() {
		List<BeautyStyleSimulation> expiredSimulations = simulationRepository.findByExpiresAtBefore(LocalDateTime.now());

		expiredSimulations.forEach(simulation -> {
			imageStorage.delete(simulation.getSourceImageUrl());
			imageStorage.delete(simulation.getStyleImageUrl());
			imageStorage.delete(simulation.getGeneratedImageUrl());
			simulation.delete();
		});

		if (!expiredSimulations.isEmpty()) {
			log.info("Cleaned up expired beauty style simulations count={}", expiredSimulations.size());
		}

		return expiredSimulations.size();
	}
}
