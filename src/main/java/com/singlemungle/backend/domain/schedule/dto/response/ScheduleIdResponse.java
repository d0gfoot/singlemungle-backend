package com.singlemungle.backend.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 ID 응답")
public record ScheduleIdResponse(
	@Schema(description = "일정 ID", example = "301")
	Long scheduleId
) {
}
