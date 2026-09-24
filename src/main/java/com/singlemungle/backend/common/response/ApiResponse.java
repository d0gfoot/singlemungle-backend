package com.singlemungle.backend.common.response;

import com.singlemungle.backend.common.exception.ErrorCode;

public record ApiResponse<T>(
	String code,
	String message,
	T data
) {

	private static final String SUCCESS_CODE = "SUCCESS";
	private static final String SUCCESS_MESSAGE = "요청 성공";

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
	}

	public static ApiResponse<Void> success() {
		return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
	}

	public static ApiResponse<Void> fail(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
	}

	public static ApiResponse<Void> fail(String code, String message) {
		return new ApiResponse<>(code, message, null);
	}
}
