package com.singlemungle.backend.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.auth.security.ApiAccessDeniedHandler;
import com.singlemungle.backend.domain.auth.security.SecurityErrorResponseWriter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class CommonExceptionHandlerTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void accessDeniedHandlerWritesCommonForbiddenResponse() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		ApiAccessDeniedHandler handler = new ApiAccessDeniedHandler(new SecurityErrorResponseWriter(objectMapper));

		handler.handle(null, response, new AccessDeniedException("denied"));

		JsonNode body = objectMapper.readTree(response.getContentAsString());
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
		assertThat(body.get("code").asText()).isEqualTo("ACCESS_DENIED");
		assertThat(body.get("data").isNull()).isTrue();
	}

	@Test
	void uploadLimitHandlerReturnsCommonPayloadTooLargeResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handlePayloadTooLarge(
			new MaxUploadSizeExceededException(1024)
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
		assertThat(response.getBody()).isEqualTo(ApiResponse.fail(ErrorCode.PAYLOAD_TOO_LARGE));
	}
}
