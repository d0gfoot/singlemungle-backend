package com.singlemungle.backend.domain.pet.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "반려동물 등록 요청")
public record PetCreateRequest(
	@Schema(description = "반려동물 이름", example = "초코")
	@NotBlank @Size(max = 50) String name,
	@Schema(description = "성별", example = "MALE")
	@Size(max = 20) String gender,
	@Schema(description = "품종", example = "푸들")
	@Size(max = 50) String breed,
	@Schema(description = "생년월일", example = "2020-01-01")
	LocalDate birthDate,
	@Schema(description = "몸무게 kg", example = "4.5")
	@Positive Double weight,
	@Schema(description = "메모", example = "활발하고 산책을 좋아함")
	@Size(max = 1000) String memo
) {
}
