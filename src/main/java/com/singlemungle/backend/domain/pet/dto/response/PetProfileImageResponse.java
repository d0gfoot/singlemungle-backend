package com.singlemungle.backend.domain.pet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "반려동물 프로필 이미지 응답")
public record PetProfileImageResponse(
	@Schema(description = "저장된 프로필 이미지 URL", example = "/api/v1/images/image.jpg")
	String profileImageUrl
) {
}
