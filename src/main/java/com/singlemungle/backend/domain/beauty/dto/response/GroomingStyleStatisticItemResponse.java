package com.singlemungle.backend.domain.beauty.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "견종별 미용 스타일 프리셋 선택 통계 항목")
public record GroomingStyleStatisticItemResponse(
	Long stylePresetId,
	String name,
	String description,
	String exampleImageUrl,
	@Schema(description = "해당 프리셋을 최근 선택한 다른 사용자 수", example = "12")
	long selectedUserCount,
	@Schema(description = "다른 사용자 전체 선택 중 비율(%)", example = "60.0")
	BigDecimal percentage
) {
}
