package com.singlemungle.backend.domain.beauty.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.singlemungle.backend.common.config.VertexAiProperties;
import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.vertex-ai.enabled", havingValue = "true")
public class VertexAiImageGenerationClient implements ImageGenerationClient {

	private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
	private static final String DEFAULT_PROMPT = "Generate a realistic dog grooming style preview image.";

	private final VertexAiProperties properties;
	private final GoogleCredentials credentials;
	private final RestClient restClient;

	public VertexAiImageGenerationClient(VertexAiProperties properties) throws IOException {
		this.properties = properties;
		this.credentials = GoogleCredentials.getApplicationDefault()
			.createScoped(List.of(CLOUD_PLATFORM_SCOPE));
		this.restClient = RestClient.create();
	}

	@Override
	public GeneratedImage generate(ImageGenerationRequest request) {
		try {
			Map<String, Object> response = restClient.post()
				.uri(vertexGenerateContentUrl())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
				.body(requestBody(request))
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

			return extractGeneratedImage(response);
		} catch (RuntimeException | IOException exception) {
			throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
		}
	}

	private String vertexGenerateContentUrl() {
		String location = properties.getLocation();
		String baseUrl = "global".equals(location)
			? "https://aiplatform.googleapis.com"
			: "https://" + location + "-aiplatform.googleapis.com";

		return "%s/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent".formatted(
			baseUrl,
			properties.getProjectId(),
			location,
			properties.getImageModel()
		);
	}

	private String accessToken() throws IOException {
		credentials.refreshIfExpired();
		AccessToken token = credentials.getAccessToken();
		if (token == null || token.getTokenValue() == null) {
			throw new IOException("Vertex AI access token is empty");
		}
		return token.getTokenValue();
	}

	private Map<String, Object> requestBody(ImageGenerationRequest request) {
		List<Map<String, Object>> parts = new ArrayList<>();
		parts.add(Map.of("text", prompt(request)));
		addInlineImage(parts, request.petImage(), request.petImageMimeType());
		addInlineImage(parts, request.styleImage(), request.styleImageMimeType());

		return Map.of(
			"contents", List.of(Map.of(
				"role", "user",
				"parts", parts
			)),
			"generationConfig", Map.of(
				"responseModalities", List.of("TEXT", "IMAGE")
			)
		);
	}

	private String prompt(ImageGenerationRequest request) {
		return request.prompt() == null || request.prompt().isBlank()
			? DEFAULT_PROMPT
			: request.prompt();
	}

	private void addInlineImage(List<Map<String, Object>> parts, byte[] image, String mimeType) {
		if (image == null || image.length == 0 || mimeType == null || mimeType.isBlank()) {
			return;
		}

		parts.add(Map.of("inlineData", Map.of(
			"mimeType", mimeType,
			"data", Base64.getEncoder().encodeToString(image)
		)));
	}

	@SuppressWarnings("unchecked")
	private GeneratedImage extractGeneratedImage(Map<String, Object> response) {
		List<Object> candidates = response == null
			? List.of()
			: asList(response.get("candidates"));

		for (Object candidateValue : candidates) {
			Map<String, Object> candidate = asMap(candidateValue);
			Map<String, Object> content = asMap(candidate.get("content"));
			for (Object partValue : asList(content.get("parts"))) {
				Map<String, Object> part = asMap(partValue);
				Map<String, Object> inlineData = asMap(part.get("inlineData"));
				if (inlineData.isEmpty()) {
					inlineData = asMap(part.get("inline_data"));
				}
				String data = asString(inlineData.get("data"));
				String mimeType = asString(inlineData.get("mimeType"));
				if (mimeType == null) {
					mimeType = asString(inlineData.get("mime_type"));
				}
				if (data != null && !data.isBlank()) {
					return new GeneratedImage(Base64.getDecoder().decode(data), mimeType);
				}
			}
		}

		throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		return value instanceof Map<?, ?> ? (Map<String, Object>)value : Map.of();
	}

	@SuppressWarnings("unchecked")
	private List<Object> asList(Object value) {
		return value instanceof List<?> ? (List<Object>)value : List.of();
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
