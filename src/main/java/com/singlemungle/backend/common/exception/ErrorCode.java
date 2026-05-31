package com.singlemungle.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청"),
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "장소를 찾을 수 없습니다"),
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
