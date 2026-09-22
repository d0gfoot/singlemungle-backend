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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
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

	private static final String PENDING_RESPONSE_EXAMPLE = """
		{
		  "code": "SUCCESS",
		  "message": "요청 성공",
		  "data": {
		    "simulationId": 42,
		    "petId": 7,
		    "shopId": 3,
		    "styleKeyword": "곰돌이컷",
		    "stylePresetId": 1,
		    "referenceReviewImageId": null,
		    "sourceType": "PRESET",
		    "additionalRequest": "좀 더 샤프하고 날렵하게",
		    "sourceImageUrl": null,
		    "styleImageUrl": null,
		    "generatedImageUrl": null,
		    "status": "PENDING",
		    "failureReason": null,
		    "expiresAt": "2026-10-22T18:00:00",
		    "createdAt": "2026-09-22T18:00:00"
		  }
		}
		""";

	private static final String COMPLETED_RESPONSE_EXAMPLE = """
		{
		  "code": "SUCCESS",
		  "message": "요청 성공",
		  "data": {
		    "simulationId": 42,
		    "petId": 7,
		    "shopId": 3,
		    "styleKeyword": "곰돌이컷",
		    "stylePresetId": 1,
		    "referenceReviewImageId": null,
		    "sourceType": "PRESET",
		    "additionalRequest": "좀 더 샤프하고 날렵하게",
		    "sourceImageUrl": null,
		    "styleImageUrl": null,
		    "generatedImageUrl": "/api/v1/beauty/style-simulations/images/generated.jpg",
		    "status": "COMPLETED",
		    "failureReason": null,
		    "expiresAt": "2026-10-22T18:00:00",
		    "createdAt": "2026-09-22T18:00:00"
		  }
		}
		""";

	private static final String FAILED_RESPONSE_EXAMPLE = """
		{
		  "code": "SUCCESS",
		  "message": "요청 성공",
		  "data": {
		    "simulationId": 42,
		    "petId": 7,
		    "shopId": 3,
		    "styleKeyword": "곰돌이컷",
		    "stylePresetId": 1,
		    "referenceReviewImageId": null,
		    "sourceType": "PRESET",
		    "additionalRequest": "좀 더 샤프하고 날렵하게",
		    "sourceImageUrl": null,
		    "styleImageUrl": null,
		    "generatedImageUrl": null,
		    "status": "FAILED",
		    "failureReason": "IMAGE_GENERATION_FAILED",
		    "expiresAt": "2026-10-22T18:00:00",
		    "createdAt": "2026-09-22T18:00:00"
		  }
		}
		""";

	private final BeautyStyleSimulationService simulationService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(
		summary = "AI 스타일 시뮬레이션 생성",
		description = "생성 요청을 접수하고 PENDING 상태를 반환합니다. 완료 여부는 상세 조회 API에서 확인합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "202",
			description = "생성 요청 접수",
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				examples = @ExampleObject(name = "PENDING", value = PENDING_RESPONSE_EXAMPLE)
			)
		)
	})
	public ApiResponse<BeautyStyleSimulationResponse> createSimulation(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@Parameter(description = "반려동물 ID", example = "101")
		@RequestParam Long petId,
		@Parameter(description = "미용샵 ID. 리뷰 이미지를 선택하면 생략할 수 있습니다.", example = "1")
		@RequestParam(required = false) Long shopId,
			@Parameter(description = "전역 미용 스타일 프리셋 ID", example = "1")
			@RequestParam(required = false) Long stylePresetId,
			@Parameter(description = "AI 참고 사용에 동의된 자체 리뷰 이미지 ID", example = "10")
			@RequestParam(required = false) Long referenceReviewImageId,
			@Parameter(description = "스타일 생성 추가 요청. 선택값입니다.", example = "좀 더 샤프하고 날렵하게")
			@RequestParam(required = false) String additionalRequest,
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
				stylePresetId,
				referenceReviewImageId,
				additionalRequest,
				petImage,
				styleImage
			));
	}

	@GetMapping("/{simulationId}")
	@Operation(summary = "AI 스타일 시뮬레이션 상세 조회", description = "내가 생성한 스타일 시뮬레이션 결과를 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "생성 상태 조회 성공",
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				examples = {
					@ExampleObject(name = "PENDING", value = PENDING_RESPONSE_EXAMPLE),
					@ExampleObject(name = "COMPLETED", value = COMPLETED_RESPONSE_EXAMPLE),
					@ExampleObject(name = "FAILED", value = FAILED_RESPONSE_EXAMPLE)
				}
			)
		)
	})
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
			.contentType(imageMediaType(fileName))
			.header(
				"Content-Disposition",
				ContentDisposition.inline()
					.filename(fileName)
					.build()
					.toString()
			)
			.body(simulationService.loadImage(fileName));
	}

	private MediaType imageMediaType(String fileName) {
		String lowerCaseFileName = fileName.toLowerCase();
		if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
			return MediaType.IMAGE_JPEG;
		}
		if (lowerCaseFileName.endsWith(".png")) {
			return MediaType.IMAGE_PNG;
		}
		if (lowerCaseFileName.endsWith(".webp")) {
			return MediaType.parseMediaType("image/webp");
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}
}
