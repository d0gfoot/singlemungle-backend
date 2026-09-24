package com.singlemungle.backend.domain.auth.oauth;

public record OAuthUserInfo(
	OAuthProvider provider,
	String providerUserId,
	String email,
	String nickname,
	String profileImageUrl
) {
}
