package com.singlemungle.backend.domain.place.controller;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.auth.security.CustomUserPrincipal;
import com.singlemungle.backend.domain.place.dto.request.FavoritePlaceCreateRequest;
import com.singlemungle.backend.domain.place.dto.request.FavoritePlaceUpdateRequest;
import com.singlemungle.backend.domain.place.dto.response.FavoritePlaceResponse;
import com.singlemungle.backend.domain.place.service.FavoritePlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/favorite-places")
@RequiredArgsConstructor
@Tag(name = "FavoritePlace", description = "내 애견 미용샵 즐겨찾기 API")
@SecurityRequirement(name = "bearerAuth")
public class FavoritePlaceController {

	private final FavoritePlaceService favoritePlaceService;

	@PostMapping
	@Operation(summary = "즐겨찾기 등록")
	public ApiResponse<FavoritePlaceResponse> create(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@Valid @RequestBody FavoritePlaceCreateRequest request
	) {
		return ApiResponse.success(favoritePlaceService.create(principal.userId(), request));
	}

	@GetMapping
	@Operation(summary = "내 즐겨찾기 목록 조회")
	public ApiResponse<PageResponse<FavoritePlaceResponse>> getMyFavorites(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		Pageable pageable
	) {
		return ApiResponse.success(favoritePlaceService.getMyFavorites(principal.userId(), pageable));
	}

	@PatchMapping("/{favoritePlaceId}")
	@Operation(summary = "즐겨찾기 메모 수정")
	public ApiResponse<FavoritePlaceResponse> update(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@PathVariable Long favoritePlaceId,
		@Valid @RequestBody FavoritePlaceUpdateRequest request
	) {
		return ApiResponse.success(favoritePlaceService.update(principal.userId(), favoritePlaceId, request));
	}

	@DeleteMapping("/{favoritePlaceId}")
	@Operation(summary = "즐겨찾기 삭제")
	public ApiResponse<Void> delete(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@PathVariable Long favoritePlaceId
	) {
		favoritePlaceService.delete(principal.userId(), favoritePlaceId);
		return ApiResponse.success();
	}
}
