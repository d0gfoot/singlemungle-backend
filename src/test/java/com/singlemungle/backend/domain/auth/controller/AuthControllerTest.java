package com.singlemungle.backend.domain.auth.controller;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singlemungle.backend.domain.auth.oauth.KakaoOAuthClient;
import com.singlemungle.backend.domain.auth.oauth.OAuthClient;
import com.singlemungle.backend.domain.auth.oauth.OAuthClientResolver;
import com.singlemungle.backend.domain.auth.oauth.OAuthProvider;
import com.singlemungle.backend.domain.auth.oauth.OAuthUserInfo;
import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.user.entity.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/db/testdata/cleanup-auth-test-data.sql")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockBean
	private OAuthClientResolver oauthClientResolver;

	@Test
	void oauthLoginCreatesUserAndReturnsTokens() throws Exception {
		OAuthClient kakaoOAuthClient = org.mockito.Mockito.mock(KakaoOAuthClient.class);
		when(oauthClientResolver.resolve(OAuthProvider.KAKAO)).thenReturn(kakaoOAuthClient);
		when(kakaoOAuthClient.fetchUserInfo(anyString())).thenReturn(new OAuthUserInfo(
			OAuthProvider.KAKAO,
			"kakao-user-1",
			"user@example.com",
			"카카오보호자",
			"https://image.example.com/profile.png"
		));

		mockMvc.perform(post("/api/v1/auth/oauth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "provider": "KAKAO",
					  "providerToken": "kakao-provider-token"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.accessToken").isString())
			.andExpect(jsonPath("$.data.refreshToken").isString())
			.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.data.expiresIn").value(3600))
			.andExpect(jsonPath("$.data.isNewUser").value(true));
	}

	@Test
	void oauthLoginReactivatesSoftDeletedUser() throws Exception {
		jdbcTemplate.update("""
			insert into users (
				id, oauth_provider, oauth_provider_id, email, nickname, role,
				created_at, updated_at, deleted_at
			) values (?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp, current_timestamp)
			""", 10L, "KAKAO", "withdrawn-kakao-user", "old@example.com", "탈퇴사용자", "USER");
		jdbcTemplate.update("""
			insert into refresh_tokens (user_id, token, expires_at, created_at)
			values (?, ?, ?, ?)
			""", 10L, "old-refresh-token", LocalDateTime.now().plusDays(1), LocalDateTime.now());

		OAuthClient kakaoOAuthClient = org.mockito.Mockito.mock(KakaoOAuthClient.class);
		when(oauthClientResolver.resolve(OAuthProvider.KAKAO)).thenReturn(kakaoOAuthClient);
		when(kakaoOAuthClient.fetchUserInfo(anyString())).thenReturn(new OAuthUserInfo(
			OAuthProvider.KAKAO,
			"withdrawn-kakao-user",
			"restored@example.com",
			"복구보호자",
			"https://image.example.com/restored.png"
		));

		mockMvc.perform(post("/api/v1/auth/oauth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "provider": "KAKAO",
					  "providerToken": "kakao-provider-token"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.isNewUser").value(false));

		Integer userCount = jdbcTemplate.queryForObject(
			"select count(*) from users where oauth_provider = 'KAKAO' and oauth_provider_id = 'withdrawn-kakao-user'",
			Integer.class
		);
		Integer activeUserCount = jdbcTemplate.queryForObject(
			"select count(*) from users where id = 10 and deleted_at is null and email = 'restored@example.com'",
			Integer.class
		);
		Integer refreshTokenCount = jdbcTemplate.queryForObject(
			"select count(*) from refresh_tokens where user_id = 10",
			Integer.class
		);

		org.assertj.core.api.Assertions.assertThat(userCount).isEqualTo(1);
		org.assertj.core.api.Assertions.assertThat(activeUserCount).isEqualTo(1);
		org.assertj.core.api.Assertions.assertThat(refreshTokenCount).isEqualTo(1);
	}

	@Test
	void logoutRequiresAccessToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_TOKEN"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void logoutDeletesRefreshTokenWhenAuthenticated() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTokenService.createAccessToken(1L, UserRole.USER)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));
	}
}
