package com.singlemungle.backend.domain.walk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "산책 기록 ID 응답")
public record WalkIdResponse(

        @Schema(description = "산책 기록 ID", example = "21")
        Long walkId
) {
}