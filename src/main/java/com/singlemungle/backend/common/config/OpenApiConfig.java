package com.singlemungle.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("싱글멍글 API")
				.description("반려동물 라이프 통합 관리 플랫폼 싱글멍글 백엔드 API 문서")
				.version("v1"));
	}
}
