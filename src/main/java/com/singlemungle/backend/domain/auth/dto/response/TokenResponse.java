package com.singlemungle.backend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 토큰 응답")
public record TokenResponse(
	@Schema(description = "보호 API 호출에 사용하는 JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.access-token")
	String accessToken,
	@Schema(description = "access token 재발급에 사용하는 JWT refresh token", example = "eyJhbGciOiJIUzI1NiJ9.refresh-token")
	String refreshToken,
	@Schema(description = "토큰 타입", example = "Bearer")
	String tokenType,
	@Schema(description = "액세스 토큰 만료까지 남은 초", example = "3600")
	long expiresIn,
	@Schema(description = "신규 가입 여부", example = "false")
	boolean isNewUser
) {

	public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, boolean isNewUser) {
		return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, isNewUser);
	}
}
