package com.singlemungle.backend.domain.beauty.dto.response;

import java.time.LocalDateTime;

import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 스타일 시뮬레이션 응답")
public record BeautyStyleSimulationResponse(
	@Schema(description = "시뮬레이션 ID", example = "1")
	Long simulationId,

	@Schema(description = "반려동물 ID", example = "101")
	Long petId,

	@Schema(description = "미용샵 ID", example = "1")
	Long shopId,

	@Schema(description = "미용 스타일 키워드", example = "곰돌이컷")
	String styleKeyword,

	@Schema(description = "스타일 생성 추가 요청", example = "좀 더 샤프하고 날렵하게")
	String additionalRequest,

	@Schema(description = "원본 반려견 이미지 URL")
	String sourceImageUrl,

	@Schema(description = "참고 스타일 이미지 URL")
	String styleImageUrl,

	@Schema(description = "생성된 스타일 이미지 URL")
	String generatedImageUrl,

	@Schema(description = "생성 상태", example = "COMPLETED")
	BeautyStyleSimulationStatus status,

	@Schema(description = "만료 시각", example = "2026-10-12T14:30:00")
	LocalDateTime expiresAt,

	@Schema(description = "생성 시각", example = "2026-09-12T14:30:00")
	LocalDateTime createdAt
) {

	public static BeautyStyleSimulationResponse from(BeautyStyleSimulation simulation) {
		return new BeautyStyleSimulationResponse(
			simulation.getId(),
				simulation.getPetId(),
				simulation.getShopId(),
				simulation.getStyleKeyword(),
				simulation.getAdditionalRequest(),
				simulation.getSourceImageUrl(),
				simulation.getStyleImageUrl(),
			simulation.getGeneratedImageUrl(),
			simulation.getStatus(),
			simulation.getExpiresAt(),
			simulation.getCreatedAt()
		);
	}
}
