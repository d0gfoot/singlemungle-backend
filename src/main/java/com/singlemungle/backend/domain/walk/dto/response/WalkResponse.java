package com.singlemungle.backend.domain.walk.dto.response;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "산책 기록 응답")
public record WalkResponse(

        @Schema(description = "산책 기록 ID", example = "21")
        Long walkId,

        @Schema(description = "산책 시작 시간", example = "2026-08-09T14:00:00")
        LocalDateTime startedAt,

        @Schema(description = "산책 종료 시간", example = "2026-08-09T14:34:00")
        LocalDateTime endedAt,

        @Schema(description = "산책 거리(km)", example = "2.6")
        Double distanceKm,

        @Schema(description = "산책 시간(분)", example = "34")
        Integer durationMinutes,

        @Schema(description = "소모 칼로리(kcal)", example = "76")
        Integer calories,

        @Schema(description = "GPS 산책 경로 JSON 데이터")
        String routeData,

        @Schema(description = "산책 메모", example = "공원에서 산책")
        String memo
) {
}