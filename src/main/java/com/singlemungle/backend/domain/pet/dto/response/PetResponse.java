package com.singlemungle.backend.domain.pet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "반려동물 응답")
public record PetResponse(
	@Schema(description = "반려동물 ID", example = "101")
	Long petId,
	@Schema(description = "반려동물 이름", example = "초코")
	String name,
	@Schema(description = "품종", example = "푸들")
	String breed,
	@Schema(description = "나이", example = "6")
	Integer age,
	@Schema(description = "대표 반려동물 여부", example = "true")
	Boolean isRepresentative
) {
}
