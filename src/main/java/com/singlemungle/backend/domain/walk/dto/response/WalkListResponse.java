package com.singlemungle.backend.domain.walk.dto.response;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "산책 기록 목록 응답")
public record WalkListResponse(

        @Schema(description = "산책 기록 목록")
        List<WalkResponse> content,

        @Schema(description = "현재 페이지", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNext
) {
}