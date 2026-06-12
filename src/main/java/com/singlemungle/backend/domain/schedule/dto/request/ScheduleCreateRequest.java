package com.singlemungle.backend.domain.schedule.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "일정 등록 요청")
public record ScheduleCreateRequest(
	@Schema(description = "반려동물 ID. 반려동물과 무관한 일정이면 생략할 수 있습니다.", example = "101")
	Long petId,
	@Schema(description = "일정 유형", example = "HOSPITAL")
	@NotBlank @Size(max = 30) String scheduleType,
	@Schema(description = "제목", example = "정기 검진")
	@NotBlank @Size(max = 100) String title,
	@Schema(description = "시작 일시", example = "2026-07-01T10:00:00")
	@NotNull LocalDateTime startAt,
	@Schema(description = "종료 일시", example = "2026-07-01T11:00:00")
	LocalDateTime endAt,
	@Schema(description = "장소", example = "사랑동물병원")
	@Size(max = 100) String location,
	@Schema(description = "메모", example = "공복 필요")
	@Size(max = 1000) String memo,
	@Schema(description = "알림 여부", example = "true")
	Boolean isAlarm
) {
}
