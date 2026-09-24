package com.singlemungle.backend.domain.beauty.ai;

public interface ImageGenerationClient {

	GeneratedImage generate(ImageGenerationRequest request);
}
