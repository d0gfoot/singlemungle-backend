package com.singlemungle.backend.domain.beauty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미용 스타일 응답")
public record BeautyStyleResponse(

        @Schema(description = "스타일 ID", example = "1")
        Long styleId,

        @Schema(description = "스타일 이름", example = "곰돌이컷")
        String styleName,

        @Schema(description = "스타일 설명", example = "얼굴을 둥글게 다듬는 스타일")
        String description,

        @Schema(description = "스타일 예시 이미지 URL",
                example = "https://example.com/style1.jpg")
        String imageUrl
) {
}