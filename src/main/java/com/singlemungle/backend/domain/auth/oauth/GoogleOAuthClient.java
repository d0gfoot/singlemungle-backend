package com.singlemungle.backend.domain.auth.oauth;

import java.util.Map;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOAuthClient implements OAuthClient {

	private final RestClient restClient = RestClient.create("https://www.googleapis.com");

	@Override
	public OAuthProvider provider() {
		return OAuthProvider.GOOGLE;
	}

	@Override
	public OAuthUserInfo fetchUserInfo(String providerToken) {
		try {
			Map<String, Object> response = restClient.get()
				.uri("/oauth2/v3/userinfo")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

			return new OAuthUserInfo(
				OAuthProvider.GOOGLE,
				asString(response.get("sub")),
				asString(response.get("email")),
				asString(response.get("name")),
				asString(response.get("picture"))
			);
		} catch (RuntimeException exception) {
			throw new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN);
		}
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
