package com.singlemungle.backend.domain.auth.security;

import java.io.IOException;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.auth.service.JwtTokenService.JwtClaims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenService jwtTokenService;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			JwtClaims claims = jwtTokenService.parseToken(authorization.substring(BEARER_PREFIX.length()));
			CustomUserPrincipal principal = new CustomUserPrincipal(claims.userId(), claims.role());
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (BusinessException exception) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}
