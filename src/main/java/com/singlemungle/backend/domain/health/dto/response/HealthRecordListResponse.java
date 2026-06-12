package com.singlemungle.backend.domain.health.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "건강 기록 목록 응답")
public record HealthRecordListResponse(
	@Schema(description = "건강 기록 ID", example = "201")
	Long recordId,
	@Schema(description = "건강 기록 유형", example = "VACCINE")
	String recordType,
	@Schema(description = "제목", example = "종합백신")
	String title,
	@Schema(description = "기록일", example = "2026-06-01")
	LocalDate recordDate
) {
}
