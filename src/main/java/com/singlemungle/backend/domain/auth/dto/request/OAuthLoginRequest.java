package com.singlemungle.backend.domain.auth.dto.request;

import com.singlemungle.backend.domain.auth.oauth.OAuthProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "SNS OAuth 로그인 요청")
public record OAuthLoginRequest(
	@Schema(description = "OAuth 제공자. KAKAO, NAVER, GOOGLE 중 하나를 사용합니다.", example = "KAKAO")
	@NotNull OAuthProvider provider,
	@Schema(
		description = "앱에서 SNS SDK 로그인 후 발급받은 provider access token 또는 id token",
		example = "kakao-access-token-from-app"
	)
	@NotBlank String providerToken
) {
}
