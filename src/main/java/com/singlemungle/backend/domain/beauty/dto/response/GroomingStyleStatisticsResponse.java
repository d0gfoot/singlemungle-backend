package com.singlemungle.backend.domain.beauty.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 반려동물 견종 기준 미용 스타일 프리셋 통계")
public record GroomingStyleStatisticsResponse(
	String breedCode,
	String breedName,
	@Schema(description = "통계에 포함된 다른 사용자 수", example = "20")
	long totalUsers,
	List<GroomingStyleStatisticItemResponse> statistics
) {
}
