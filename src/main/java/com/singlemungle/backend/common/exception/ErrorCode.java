package com.singlemungle.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청"),
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "장소를 찾을 수 없습니다"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
	PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_NOT_FOUND", "반려동물을 찾을 수 없습니다"),
	HEALTH_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "HEALTH_RECORD_NOT_FOUND", "건강 기록을 찾을 수 없습니다"),
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "일정을 찾을 수 없습니다"),
	UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "UNSUPPORTED_OAUTH_PROVIDER", "지원하지 않는 OAuth 제공자입니다"),
	INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_OAUTH_TOKEN", "유효하지 않은 OAuth 토큰입니다"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다"),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND", "리프레시 토큰을 찾을 수 없습니다"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
