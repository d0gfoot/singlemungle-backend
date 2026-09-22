package com.singlemungle.backend.domain.review.controller;

import java.util.List;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.auth.security.CustomUserPrincipal;
import com.singlemungle.backend.domain.review.dto.request.ReviewUpdateRequest;
import com.singlemungle.backend.domain.review.dto.response.ReviewResponse;
import com.singlemungle.backend.domain.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/beauty-shops/{shopId}/reviews")
@RequiredArgsConstructor
@Tag(name = "BeautyReview", description = "미용샵 자체 리뷰 API")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "미용샵 리뷰 작성")
	public ApiResponse<ReviewResponse> create(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@PathVariable Long shopId,
		@RequestParam int rating,
		@RequestParam String content,
		@RequestParam(required = false) String groomingStyleName,
		@RequestParam(defaultValue = "false") boolean aiReferenceAllowed,
		@RequestPart(required = false) List<MultipartFile> images
	) {
		return ApiResponse.success(reviewService.create(
			principal.userId(), shopId, rating, content, groomingStyleName, aiReferenceAllowed, images
		));
	}

	@GetMapping
	@Operation(summary = "미용샵 리뷰 목록 조회")
	public ApiResponse<PageResponse<ReviewResponse>> getReviews(@PathVariable Long shopId, Pageable pageable) {
		return ApiResponse.success(reviewService.getReviews(shopId, pageable));
	}

	@GetMapping("/{reviewId}")
	@Operation(summary = "미용샵 리뷰 상세 조회")
	public ApiResponse<ReviewResponse> getReview(@PathVariable Long shopId, @PathVariable Long reviewId) {
		return ApiResponse.success(reviewService.getReview(shopId, reviewId));
	}

	@PatchMapping("/{reviewId}")
	@Operation(summary = "내 미용샵 리뷰 수정")
	public ApiResponse<ReviewResponse> update(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@PathVariable Long shopId,
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewUpdateRequest request
	) {
		return ApiResponse.success(reviewService.update(principal.userId(), shopId, reviewId, request));
	}

	@DeleteMapping("/{reviewId}")
	@Operation(summary = "내 미용샵 리뷰 삭제")
	public ApiResponse<Void> delete(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@PathVariable Long shopId,
		@PathVariable Long reviewId
	) {
		reviewService.delete(principal.userId(), shopId, reviewId);
		return ApiResponse.success();
	}
}
