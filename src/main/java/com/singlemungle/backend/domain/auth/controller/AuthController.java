package com.singlemungle.backend.domain.auth.controller;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.auth.dto.request.OAuthLoginRequest;
import com.singlemungle.backend.domain.auth.dto.request.TokenReissueRequest;
import com.singlemungle.backend.domain.auth.dto.response.TokenResponse;
import com.singlemungle.backend.domain.auth.security.CustomUserPrincipal;
import com.singlemungle.backend.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "OAuth 로그인 및 JWT API")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/oauth/login")
	@Operation(
		summary = "SNS OAuth 로그인",
		description = """
			React Native 앱에서 Kakao/Naver/Google SDK 로그인 후 발급받은 provider token을 전달합니다.
			백엔드는 provider userinfo API로 토큰을 검증하고, provider user id 기준으로 회원을 조회하거나 생성한 뒤 서비스 JWT를 발급합니다.
			email은 provider가 제공할 때만 저장하며 필수값이 아닙니다.
			"""
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "로그인 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SUCCESS",
					  "message": "요청 성공",
					  "data": {
					    "accessToken": "eyJhbGciOiJIUzI1NiJ9.access",
					    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh",
					    "tokenType": "Bearer",
					    "expiresIn": 3600,
					    "isNewUser": true
					  }
					}
					""")
			)
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "지원하지 않는 provider 또는 요청 형식 오류",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(value = """
					{
					  "code": "UNSUPPORTED_OAUTH_PROVIDER",
					  "message": "지원하지 않는 OAuth 제공자입니다",
					  "data": null
					}
					""")
			)
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "provider token 검증 실패",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(value = """
					{
					  "code": "INVALID_OAUTH_TOKEN",
					  "message": "유효하지 않은 OAuth 토큰입니다",
					  "data": null
					}
					""")
			)
		)
	})
	public ApiResponse<TokenResponse> login(@Valid @RequestBody OAuthLoginRequest request) {
		return ApiResponse.success(authService.login(request));
	}

	@PostMapping("/reissue")
	@Operation(
		summary = "JWT 재발급",
		description = """
			refresh token으로 새 access token과 refresh token을 발급합니다.
			재발급에 성공하면 기존 refresh token은 삭제되고 새 refresh token만 저장됩니다.
			"""
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "재발급 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SUCCESS",
					  "message": "요청 성공",
					  "data": {
					    "accessToken": "eyJhbGciOiJIUzI1NiJ9.new-access",
					    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.new-refresh",
					    "tokenType": "Bearer",
					    "expiresIn": 3600,
					    "isNewUser": false
					  }
					}
					""")
			)
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "요청 형식 오류",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(value = """
					{
					  "code": "INVALID_REQUEST",
					  "message": "잘못된 요청",
					  "data": null
					}
					""")
			)
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "refresh token 없음 또는 만료",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(value = """
					{
					  "code": "REFRESH_TOKEN_NOT_FOUND",
					  "message": "리프레시 토큰을 찾을 수 없습니다",
					  "data": null
					}
					""")
			)
		)
	})
	public ApiResponse<TokenResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
		return ApiResponse.success(authService.reissue(request.refreshToken()));
	}

	@PostMapping("/logout")
	@Operation(
		summary = "로그아웃",
		description = "Authorization 헤더의 access token으로 현재 사용자를 식별하고, 해당 사용자의 refresh token을 삭제합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "로그아웃 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SUCCESS",
					  "message": "요청 성공",
					  "data": null
					}
					""")
			)
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "Authorization 헤더 없음 또는 유효하지 않은 access token",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(value = """
					{
					  "code": "INVALID_TOKEN",
					  "message": "유효하지 않은 인증 토큰입니다",
					  "data": null
					}
					""")
			)
		)
	})
	public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {
		authService.logout(principal.userId());
		return ApiResponse.success();
	}
}
