package com.singlemungle.backend.domain.beauty.controller;

import java.util.List;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.beauty.dto.response.GroomingStylePresetResponse;
import com.singlemungle.backend.domain.beauty.service.GroomingStylePresetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/beauty/style-presets")
@RequiredArgsConstructor
@Tag(name = "GroomingStylePreset", description = "공통 미용 스타일 프리셋 API")
@SecurityRequirement(name = "bearerAuth")
public class GroomingStylePresetController {

	private final GroomingStylePresetService presetService;

	@GetMapping
	@Operation(summary = "공통 미용 스타일 프리셋 목록 조회")
	public ApiResponse<List<GroomingStylePresetResponse>> getPresets() {
		return ApiResponse.success(presetService.getPresets());
	}
}
