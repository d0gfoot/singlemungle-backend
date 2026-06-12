package com.singlemungle.backend.domain.health.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "건강 기록 등록 요청")
public record HealthRecordCreateRequest(
	@Schema(description = "건강 기록 유형", example = "VACCINE")
	@NotBlank @Size(max = 30) String recordType,
	@Schema(description = "제목", example = "종합백신")
	@NotBlank @Size(max = 100) String title,
	@Schema(description = "병원명", example = "사랑동물병원")
	@Size(max = 100) String hospitalName,
	@Schema(description = "기록일", example = "2026-06-01")
	@NotNull LocalDate recordDate,
	@Schema(description = "상세 설명", example = "종합백신 접종 완료")
	@Size(max = 2000) String description,
	@Schema(description = "다음 예정일", example = "2026-12-01")
	LocalDate nextScheduleDate
) {
}
