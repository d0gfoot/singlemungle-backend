package com.singlemungle.backend.domain.beauty.ai;

public record GroomingStyleContext(
	String displayName,
	String promptDescription,
	String negativePrompt
) {
}
