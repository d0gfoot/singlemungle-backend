package com.singlemungle.backend.domain.health.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "건강 기록 상세 응답")
public record HealthRecordDetailResponse(
	@Schema(description = "건강 기록 ID", example = "201")
	Long recordId,
	@Schema(description = "건강 기록 유형", example = "VACCINE")
	String recordType,
	@Schema(description = "제목", example = "종합백신")
	String title,
	@Schema(description = "병원명", example = "사랑동물병원")
	String hospitalName,
	@Schema(description = "기록일", example = "2026-06-01")
	LocalDate recordDate,
	@Schema(description = "상세 설명", example = "종합백신 접종 완료")
	String description
) {
}
