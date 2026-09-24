package com.singlemungle.backend.domain.auth.service;

import java.util.Date;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.user.entity.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

	private static final String ROLE_CLAIM = "role";

	private final javax.crypto.SecretKey secretKey;
	private final long accessTokenValiditySeconds;
	private final long refreshTokenValiditySeconds;

	public JwtTokenService(
		@Value("${app.jwt.secret}") String secret,
		@Value("${app.jwt.access-token-validity-seconds}") long accessTokenValiditySeconds,
		@Value("${app.jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	public String createAccessToken(Long userId, UserRole role) {
		return createToken(userId, role, accessTokenValiditySeconds);
	}

	public String createRefreshToken(Long userId, UserRole role) {
		return createToken(userId, role, refreshTokenValiditySeconds);
	}

	public long getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	public long getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	public JwtClaims parseToken(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			return new JwtClaims(
				Long.valueOf(claims.getSubject()),
				UserRole.valueOf(claims.get(ROLE_CLAIM, String.class)),
				claims.getExpiration().toInstant().getEpochSecond()
			);
		} catch (JwtException | IllegalArgumentException exception) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}
	}

	private String createToken(Long userId, UserRole role, long validitySeconds) {
		Date now = new Date();
		Date expiresAt = new Date(now.getTime() + validitySeconds * 1000);

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim(ROLE_CLAIM, role.name())
			.issuedAt(now)
			.expiration(expiresAt)
			.signWith(secretKey)
			.compact();
	}

	public record JwtClaims(
		Long userId,
		UserRole role,
		long expiresAt
	) {
	}
}
