package com.singlemungle.backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record TokenReissueRequest(
	@Schema(
		description = "로그인 또는 이전 재발급 응답으로 받은 refresh token",
		example = "eyJhbGciOiJIUzI1NiJ9.refresh-token"
	)
	@NotBlank String refreshToken
) {
}
