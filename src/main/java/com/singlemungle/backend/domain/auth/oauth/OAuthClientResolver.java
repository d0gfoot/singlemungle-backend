package com.singlemungle.backend.domain.auth.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.springframework.stereotype.Component;

@Component
public class OAuthClientResolver {

	private final Map<OAuthProvider, OAuthClient> clients;

	public OAuthClientResolver(List<OAuthClient> clients) {
		this.clients = clients.stream()
			.collect(Collectors.toMap(OAuthClient::provider, Function.identity()));
	}

	public OAuthClient resolve(OAuthProvider provider) {
		OAuthClient client = clients.get(provider);
		if (client == null) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
		}
		return client;
	}
}
