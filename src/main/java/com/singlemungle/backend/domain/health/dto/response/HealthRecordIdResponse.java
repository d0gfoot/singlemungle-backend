package com.singlemungle.backend.domain.health.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "건강 기록 ID 응답")
public record HealthRecordIdResponse(
	@Schema(description = "건강 기록 ID", example = "201")
	Long recordId
) {
}
