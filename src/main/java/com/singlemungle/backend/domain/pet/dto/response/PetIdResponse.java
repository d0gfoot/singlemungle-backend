package com.singlemungle.backend.domain.pet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "반려동물 ID 응답")
public record PetIdResponse(
	@Schema(description = "반려동물 ID", example = "101")
	Long petId
) {
}
