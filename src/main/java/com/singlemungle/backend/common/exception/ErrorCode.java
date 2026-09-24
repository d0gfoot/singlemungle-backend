package com.singlemungle.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청"),
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_NOT_FOUND", "장소를 찾을 수 없습니다"),
	FAVORITE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "FAVORITE_PLACE_NOT_FOUND", "즐겨찾기를 찾을 수 없습니다"),
	FAVORITE_PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "FAVORITE_PLACE_ALREADY_EXISTS", "이미 즐겨찾기에 등록된 장소입니다"),
	PLACE_PROVIDER_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "PLACE_PROVIDER_UNAVAILABLE", "장소 제공자 연결에 실패했습니다"),
	PLACE_PROVIDER_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "PLACE_PROVIDER_TIMEOUT", "장소 제공자 응답 시간이 초과되었습니다"),
	PLACE_PROVIDER_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "PLACE_PROVIDER_RATE_LIMITED", "장소 조회 요청이 너무 많습니다"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
	PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_NOT_FOUND", "반려동물을 찾을 수 없습니다"),
	HEALTH_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "HEALTH_RECORD_NOT_FOUND", "건강 기록을 찾을 수 없습니다"),
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "일정을 찾을 수 없습니다"),
	STYLE_SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "STYLE_SIMULATION_NOT_FOUND", "스타일 시뮬레이션 결과를 찾을 수 없습니다"),
	STYLE_PRESET_NOT_FOUND(HttpStatus.NOT_FOUND, "STYLE_PRESET_NOT_FOUND", "미용 스타일 프리셋을 찾을 수 없습니다"),
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다"),
	REVIEW_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_IMAGE_NOT_FOUND", "리뷰 이미지를 찾을 수 없습니다"),
	AI_REFERENCE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AI_REFERENCE_NOT_ALLOWED", "AI 참고 이미지 사용에 동의하지 않은 이미지입니다"),
	UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "UNSUPPORTED_OAUTH_PROVIDER", "지원하지 않는 OAuth 제공자입니다"),
	INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_OAUTH_TOKEN", "유효하지 않은 OAuth 토큰입니다"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다"),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND", "리프레시 토큰을 찾을 수 없습니다"),
	IMAGE_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "IMAGE_GENERATION_FAILED", "이미지 생성에 실패했습니다"),
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
