package com.singlemungle.backend.domain.beauty.service;

import com.singlemungle.backend.domain.beauty.ai.ImageGenerationRequest;

public record BeautyStyleSimulationRequestedEvent(
	Long simulationId,
	String sourceImageUrl,
	String styleImageUrl,
	ImageGenerationRequest generationRequest
) {
}
