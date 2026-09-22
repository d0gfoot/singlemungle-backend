package com.singlemungle.backend.domain.beauty.service;

import com.singlemungle.backend.domain.beauty.storage.TemporaryImage;

public record BeautyStyleSimulationRequestedEvent(
	Long simulationId,
	String prompt,
	TemporaryImage petImage,
	TemporaryImage styleImage
) {
}
