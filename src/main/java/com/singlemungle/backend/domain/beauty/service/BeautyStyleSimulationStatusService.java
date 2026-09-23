package com.singlemungle.backend.domain.beauty.service;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeautyStyleSimulationStatusService {

	private final BeautyStyleSimulationRepository simulationRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean complete(Long simulationId, String generatedImageUrl) {
		return findSimulation(simulationId).complete(generatedImageUrl);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean fail(Long simulationId, String failureReason) {
		return findSimulation(simulationId).fail(failureReason);
	}

	private BeautyStyleSimulation findSimulation(Long simulationId) {
		return simulationRepository.findById(simulationId)
			.orElseThrow(() -> new BusinessException(ErrorCode.STYLE_SIMULATION_NOT_FOUND));
	}
}
