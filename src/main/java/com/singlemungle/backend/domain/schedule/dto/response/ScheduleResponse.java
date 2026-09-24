package com.singlemungle.backend.domain.schedule.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 응답")
public record ScheduleResponse(
	@Schema(description = "일정 ID", example = "301")
	Long scheduleId,
	@Schema(description = "반려동물 ID", example = "101")
	Long petId,
	@Schema(description = "일정 유형", example = "HOSPITAL")
	String scheduleType,
	@Schema(description = "제목", example = "정기 검진")
	String title,
	@Schema(description = "시작 일시", example = "2026-07-01T10:00:00")
	LocalDateTime startAt,
	@Schema(description = "종료 일시", example = "2026-07-01T11:00:00")
	LocalDateTime endAt,
	@Schema(description = "장소", example = "사랑동물병원")
	String location,
	@Schema(description = "메모", example = "공복 필요")
	String memo,
	@Schema(description = "알림 여부", example = "true")
	Boolean isAlarm
) {
}
