package com.singlemungle.backend.domain.walk.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "산책 기록 등록 요청")
public record WalkCreateRequest(

        @Schema(
                description = "산책 시작 시간",
                example = "2026-08-09T14:00:00"
        )
        @NotNull
        LocalDateTime startedAt,

        @Schema(
                description = "산책 종료 시간",
                example = "2026-08-09T14:34:00"
        )
        @NotNull
        LocalDateTime endedAt,

        @Schema(
                description = "산책 거리(km)",
                example = "2.6"
        )
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        Double distanceKm,

        @Schema(
                description = "산책 시간(분)",
                example = "34"
        )
        @NotNull
        @Min(1)
        Integer durationMinutes,

        @Schema(description = "GPS 산책 경로")
        List<RoutePoint> routeData,

        @Schema(
                description = "산책 메모",
                example = "공원에서 산책"
        )
        String memo
) {
        public record RoutePoint(
                Double latitude,
                Double longitude
        ) {
        }
}