package com.singlemungle.backend.domain.health.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "건강 기록 수정 요청")
public record HealthRecordUpdateRequest(
	@Schema(description = "건강 기록 유형", example = "HOSPITAL")
	@Size(max = 30) String recordType,
	@Schema(description = "제목", example = "정기 검진")
	@Size(max = 100) String title,
	@Schema(description = "병원명", example = "사랑동물병원")
	@Size(max = 100) String hospitalName,
	@Schema(description = "기록일", example = "2026-06-02")
	LocalDate recordDate,
	@Schema(description = "상세 설명", example = "검진 결과 특이사항 없음")
	@Size(max = 2000) String description,
	@Schema(description = "다음 예정일", example = "2026-12-02")
	LocalDate nextScheduleDate
) {
}
