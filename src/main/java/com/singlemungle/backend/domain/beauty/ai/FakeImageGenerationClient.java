package com.singlemungle.backend.domain.beauty.ai;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.vertex-ai.enabled", havingValue = "false", matchIfMissing = true)
public class FakeImageGenerationClient implements ImageGenerationClient {

	@Override
	public GeneratedImage generate(ImageGenerationRequest request) {
		return new GeneratedImage("fake-generated-image".getBytes(StandardCharsets.UTF_8), "text/plain");
	}
}
