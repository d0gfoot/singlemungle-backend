package com.singlemungle.backend.domain.beauty.ai;

public record ImageGenerationRequest(
	String prompt,
	byte[] petImage,
	String petImageMimeType,
	byte[] styleImage,
	String styleImageMimeType
) {
}
