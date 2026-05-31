package com.singlemungle.backend.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ApiResponseJsonTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void successResponseContainsCodeMessageAndData() throws Exception {
		TestPlaceResponse placeResponse = new TestPlaceResponse(1L);

		String json = objectMapper.writeValueAsString(ApiResponse.success(placeResponse));

		assertThat(json).isEqualTo("""
			{"code":"SUCCESS","message":"요청 성공","data":{"placeId":1}}""");
	}

	@Test
	void failResponseContainsNullData() throws Exception {
		String json = objectMapper.writeValueAsString(ApiResponse.fail(ErrorCode.INVALID_REQUEST));

		assertThat(json).isEqualTo("""
			{"code":"INVALID_REQUEST","message":"잘못된 요청","data":null}""");
	}

	@Test
	void pageResponseContainsContentPageSizeAndHasNext() throws Exception {
		PageResponse<TestPlaceResponse> pageResponse = PageResponse.from(
			new PageImpl<>(
				List.of(new TestPlaceResponse(1L), new TestPlaceResponse(2L)),
				PageRequest.of(0, 2),
				3
			)
		);

		String json = objectMapper.writeValueAsString(ApiResponse.success(pageResponse));

		assertThat(json).isEqualTo("""
			{"code":"SUCCESS","message":"요청 성공","data":{"content":[{"placeId":1},{"placeId":2}],"page":0,"size":2,"hasNext":true}}""");
	}

	private record TestPlaceResponse(Long placeId) {
	}
}
