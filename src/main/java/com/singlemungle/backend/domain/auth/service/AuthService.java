package com.singlemungle.backend.domain.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.auth.dto.request.OAuthLoginRequest;
import com.singlemungle.backend.domain.auth.dto.response.TokenResponse;
import com.singlemungle.backend.domain.auth.entity.RefreshToken;
import com.singlemungle.backend.domain.auth.oauth.OAuthClient;
import com.singlemungle.backend.domain.auth.oauth.OAuthClientResolver;
import com.singlemungle.backend.domain.auth.oauth.OAuthUserInfo;
import com.singlemungle.backend.domain.auth.repository.RefreshTokenRepository;
import com.singlemungle.backend.domain.user.entity.User;
import com.singlemungle.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final OAuthClientResolver oauthClientResolver;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenService jwtTokenService;

	@Transactional
	public TokenResponse login(OAuthLoginRequest request) {
		OAuthClient oauthClient = oauthClientResolver.resolve(request.provider());
		OAuthUserInfo userInfo = oauthClient.fetchUserInfo(request.providerToken());

		Optional<User> existingUser = userRepository.findByOauthProviderAndOauthProviderIdAndDeletedAtIsNull(
			userInfo.provider(),
			userInfo.providerUserId()
		);
		boolean isNewUser = existingUser.isEmpty();
		User user = existingUser
			.orElseGet(() -> userRepository.save(User.createOAuthUser(
				userInfo.provider(),
				userInfo.providerUserId(),
				userInfo.email(),
				userInfo.nickname(),
				userInfo.profileImageUrl()
			)));

		if (!isNewUser) {
			user.updateOAuthProfile(userInfo.email(), userInfo.nickname(), userInfo.profileImageUrl());
		}
		return issueTokens(user, isNewUser);
	}

	@Transactional
	public TokenResponse reissue(String refreshTokenValue) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
			.orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
		if (refreshToken.isExpired(LocalDateTime.now())) {
			refreshTokenRepository.delete(refreshToken);
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}

		User user = userRepository.findByIdAndDeletedAtIsNull(refreshToken.getUserId())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		refreshTokenRepository.delete(refreshToken);
		return issueTokens(user, false);
	}

	@Transactional
	public void logout(Long userId) {
		refreshTokenRepository.deleteByUserId(userId);
	}

	private TokenResponse issueTokens(User user, boolean isNewUser) {
		String accessToken = jwtTokenService.createAccessToken(user.getId(), user.getRole());
		String refreshToken = jwtTokenService.createRefreshToken(user.getId(), user.getRole());
		refreshTokenRepository.deleteByUserId(user.getId());
		refreshTokenRepository.save(new RefreshToken(
			user.getId(),
			refreshToken,
			LocalDateTime.now().plusSeconds(jwtTokenService.getRefreshTokenValiditySeconds())
		));
		return TokenResponse.of(accessToken, refreshToken, jwtTokenService.getAccessTokenValiditySeconds(), isNewUser);
	}
}
