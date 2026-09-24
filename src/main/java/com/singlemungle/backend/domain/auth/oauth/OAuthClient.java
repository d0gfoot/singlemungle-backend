package com.singlemungle.backend.domain.auth.oauth;

public interface OAuthClient {

	OAuthProvider provider();

	OAuthUserInfo fetchUserInfo(String providerToken);
}
