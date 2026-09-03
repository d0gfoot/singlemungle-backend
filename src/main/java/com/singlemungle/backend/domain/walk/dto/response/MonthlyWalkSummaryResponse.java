package com.singlemungle.backend.domain.walk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월간 산책 통계 응답")
public record MonthlyWalkSummaryResponse(

        @Schema(description = "연도", example = "2026")
        int year,

        @Schema(description = "월", example = "8")
        int month,

        @Schema(description = "산책 횟수", example = "15")
        long walkCount,

        @Schema(description = "누적 산책 거리(km)", example = "34.8")
        double totalDistanceKm,

        @Schema(description = "누적 산책 시간(분)", example = "428")
        long totalDurationMinutes,

        @Schema(description = "누적 소모 칼로리(kcal)", example = "984.0")
        double totalCalorie
) {
}