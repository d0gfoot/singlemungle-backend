package com.singlemungle.backend.domain.beauty.dto.response;

import com.singlemungle.backend.domain.beauty.entity.GroomingStylePreset;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 미용 스타일 프리셋")
public record GroomingStylePresetResponse(
	Long stylePresetId,
	String name,
	String description,
	String exampleImageUrl
) {

	public static GroomingStylePresetResponse from(GroomingStylePreset preset) {
		return new GroomingStylePresetResponse(
			preset.getId(),
			preset.getName(),
			preset.getDescription(),
			preset.getExampleImageUrl()
		);
	}
}
