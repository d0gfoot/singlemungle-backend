package com.singlemungle.backend.domain.beauty.controller;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.auth.security.CustomUserPrincipal;
import com.singlemungle.backend.domain.beauty.dto.response.BeautyStyleSimulationResponse;
import com.singlemungle.backend.domain.beauty.service.BeautyStyleSimulationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/beauty/style-simulations")
@RequiredArgsConstructor
@Tag(name = "BeautyStyleSimulation", description = "AI 스타일 시뮬레이션 API")
@SecurityRequirement(name = "bearerAuth")
public class BeautyStyleSimulationController {

	private final BeautyStyleSimulationService simulationService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
		summary = "AI 스타일 시뮬레이션 생성",
		description = "반려견 이미지와 미용 스타일 키워드를 기반으로 AI 스타일 이미지를 생성합니다."
	)
	public ApiResponse<BeautyStyleSimulationResponse> createSimulation(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@Parameter(description = "반려동물 ID", example = "101")
		@RequestParam Long petId,
		@Parameter(description = "미용샵 ID", example = "1")
		@RequestParam Long shopId,
		@Parameter(description = "미용 스타일 키워드", example = "곰돌이컷")
		@RequestParam String styleKeyword,
		@Parameter(
			description = "사용자 반려견 이미지. jpg, png, webp만 허용합니다.",
			content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
		)
		@RequestPart MultipartFile petImage,
		@Parameter(
			description = "참고 스타일 이미지. 선택값입니다.",
			content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
		)
		@RequestPart(required = false) MultipartFile styleImage
	) {
		return ApiResponse.success(simulationService.createSimulation(
			principal.userId(),
			petId,
			shopId,
			styleKeyword,
			petImage,
			styleImage
		));
	}

	@GetMapping("/{simulationId}")
	@Operation(summary = "AI 스타일 시뮬레이션 상세 조회", description = "내가 생성한 스타일 시뮬레이션 결과를 조회합니다.")
	public ApiResponse<BeautyStyleSimulationResponse> getSimulation(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@Parameter(description = "시뮬레이션 ID", example = "1") @PathVariable Long simulationId
	) {
		return ApiResponse.success(simulationService.getSimulation(principal.userId(), simulationId));
	}

	@GetMapping
	@Operation(summary = "내 AI 스타일 시뮬레이션 목록 조회", description = "만료되지 않은 내 스타일 시뮬레이션 결과를 조회합니다.")
	public ApiResponse<PageResponse<BeautyStyleSimulationResponse>> getSimulations(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		Pageable pageable
	) {
		return ApiResponse.success(simulationService.getSimulations(principal.userId(), pageable));
	}

	@DeleteMapping("/{simulationId}")
	@Operation(summary = "AI 스타일 시뮬레이션 삭제", description = "내가 생성한 스타일 시뮬레이션 결과를 soft delete 처리합니다.")
	public ApiResponse<Void> deleteSimulation(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@Parameter(description = "시뮬레이션 ID", example = "1") @PathVariable Long simulationId
	) {
		simulationService.deleteSimulation(principal.userId(), simulationId);
		return ApiResponse.success();
	}

	@GetMapping("/images/{fileName}")
	@Operation(
		summary = "AI 스타일 시뮬레이션 이미지 조회",
		description = "생성 또는 업로드된 로컬 저장 이미지를 조회합니다.",
		parameters = @Parameter(name = "fileName", in = ParameterIn.PATH, schema = @Schema(type = "string"))
	)
	public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.body(simulationService.loadImage(fileName));
	}
}
