package com.singlemungle.backend.domain.schedule.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "일정 수정 요청")
public record ScheduleUpdateRequest(
	@Schema(description = "반려동물 ID", example = "101")
	Long petId,
	@Schema(description = "일정 유형", example = "BEAUTY")
	@Size(max = 30) String scheduleType,
	@Schema(description = "제목", example = "미용 예약")
	@Size(max = 100) String title,
	@Schema(description = "시작 일시", example = "2026-07-02T11:00:00")
	LocalDateTime startAt,
	@Schema(description = "종료 일시", example = "2026-07-02T12:00:00")
	LocalDateTime endAt,
	@Schema(description = "장소", example = "몽실몽실펫살롱")
	@Size(max = 100) String location,
	@Schema(description = "메모", example = "발톱 정리 포함")
	@Size(max = 1000) String memo,
	@Schema(description = "알림 여부", example = "true")
	Boolean isAlarm
) {
}
