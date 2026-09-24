package com.singlemungle.backend.common.logging;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.logging.request.enabled", havingValue = "true", matchIfMissing = true)
public class RequestLoggingFilter extends OncePerRequestFilter {

	public static final String TRACE_ID_HEADER = "X-Trace-Id";
	private static final String TRACE_ID_MDC_KEY = "traceId";
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String traceId = resolveTraceId(request);
		long startTime = System.currentTimeMillis();

		MDC.put(TRACE_ID_MDC_KEY, traceId);
		response.setHeader(TRACE_ID_HEADER, traceId);

		try {
			log.info("REQUEST_START method={} uri={} query={} remoteAddr={}",
				request.getMethod(),
				request.getRequestURI(),
				request.getQueryString(),
				request.getRemoteAddr()
			);

			filterChain.doFilter(request, response);
		} finally {
			long elapsedTime = System.currentTimeMillis() - startTime;

			log.info("REQUEST_END method={} uri={} status={} elapsedMs={}",
				request.getMethod(),
				request.getRequestURI(),
				response.getStatus(),
				elapsedTime
			);
			MDC.remove(TRACE_ID_MDC_KEY);
		}
	}

	private String resolveTraceId(HttpServletRequest request) {
		String traceId = request.getHeader(TRACE_ID_HEADER);
		if (traceId == null || traceId.isBlank()) {
			return UUID.randomUUID().toString();
		}
		return traceId;
	}
}
