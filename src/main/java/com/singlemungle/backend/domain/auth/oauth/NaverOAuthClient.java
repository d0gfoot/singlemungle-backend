package com.singlemungle.backend.domain.auth.oauth;

import java.util.Map;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NaverOAuthClient implements OAuthClient {

	private final RestClient restClient = RestClient.create("https://openapi.naver.com");

	@Override
	public OAuthProvider provider() {
		return OAuthProvider.NAVER;
	}

	@Override
	public OAuthUserInfo fetchUserInfo(String providerToken) {
		try {
			Map<String, Object> response = restClient.get()
				.uri("/v1/nid/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

			Map<String, Object> profile = asMap(response.get("response"));

			return new OAuthUserInfo(
				OAuthProvider.NAVER,
				asString(profile.get("id")),
				asString(profile.get("email")),
				asString(profile.get("nickname")),
				asString(profile.get("profile_image"))
			);
		} catch (RuntimeException exception) {
			throw new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		return value instanceof Map<?, ?> ? (Map<String, Object>)value : Map.of();
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
