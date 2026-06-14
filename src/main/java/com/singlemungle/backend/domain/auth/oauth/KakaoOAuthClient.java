package com.singlemungle.backend.domain.auth.oauth;

import java.util.Map;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthClient implements OAuthClient {

	private final RestClient restClient = RestClient.create("https://kapi.kakao.com");

	@Override
	public OAuthProvider provider() {
		return OAuthProvider.KAKAO;
	}

	@Override
	public OAuthUserInfo fetchUserInfo(String providerToken) {
		try {
			Map<String, Object> response = restClient.get()
				.uri("/v2/user/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

			Map<String, Object> kakaoAccount = asMap(response.get("kakao_account"));
			Map<String, Object> profile = asMap(kakaoAccount.get("profile"));

			return new OAuthUserInfo(
				OAuthProvider.KAKAO,
				String.valueOf(response.get("id")),
				asString(kakaoAccount.get("email")),
				asString(profile.get("nickname")),
				asString(profile.get("profile_image_url"))
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
