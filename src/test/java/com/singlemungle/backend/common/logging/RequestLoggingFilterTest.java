package com.singlemungle.backend.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

	private final RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();

	@Test
	void addsTraceIdHeaderWhenRequestDoesNotHaveTraceId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = (servletRequest, servletResponse) -> {
		};

		requestLoggingFilter.doFilter(request, response, filterChain);

		assertThat(response.getHeader(RequestLoggingFilter.TRACE_ID_HEADER)).isNotBlank();
	}

	@Test
	void keepsClientTraceIdWhenRequestHasTraceId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader(RequestLoggingFilter.TRACE_ID_HEADER, "client-trace-id");
		FilterChain filterChain = (servletRequest, servletResponse) -> {
		};

		requestLoggingFilter.doFilter(request, response, filterChain);

		assertThat(response.getHeader(RequestLoggingFilter.TRACE_ID_HEADER)).isEqualTo("client-trace-id");
	}
}
