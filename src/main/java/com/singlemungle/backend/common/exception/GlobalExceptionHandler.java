package com.singlemungle.backend.common.exception;

import com.singlemungle.backend.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler({
		MethodArgumentNotValidException.class,
		HandlerMethodValidationException.class,
		IllegalArgumentException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception exception) {
		return ResponseEntity
			.status(ErrorCode.INVALID_REQUEST.getStatus())
			.body(ApiResponse.fail(ErrorCode.INVALID_REQUEST));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
			.body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
