package com.singlemungle.backend.domain.pet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "반려동물 응답")
public record PetResponse(
	@Schema(description = "반려동물 ID", example = "101")
	Long petId,
	@Schema(description = "반려동물 이름", example = "초코")
	String name,
	@Schema(description = "품종", example = "푸들")
	String breed,
    @Schema(description = "성별", example = "MALE")
    String gender,
    @Schema(description = "생년월일", example = "2020-01-01")
    LocalDate birthDate,
    @Schema(description = "체중(kg)", example = "4.5")
    Double weight,
    @Schema(description = "메모", example = "활발하고 산책을 좋아함")
    String memo,
    @Schema(description = "중성화 여부", example = "true")
    Boolean neutered,
	@Schema(description = "나이", example = "6")
	Integer age,
	@Schema(description = "대표 반려동물 여부", example = "true")
	Boolean isRepresentative,
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/pet/profile.jpg")
    String profileImageUrl
) {
}
