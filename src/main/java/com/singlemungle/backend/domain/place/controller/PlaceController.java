package com.singlemungle.backend.domain.place.controller;

import java.math.BigDecimal;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.place.dto.NearbyPlacesResponse;
import com.singlemungle.backend.domain.place.dto.PlaceDetailResponse;
import com.singlemungle.backend.domain.place.dto.PlacePhotoUriResponse;
import com.singlemungle.backend.domain.place.service.PlaceQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "Google Places 기반 애견 미용샵 API")
public class PlaceController {

	private final PlaceQueryService placeQueryService;

	@GetMapping("/nearby")
	@Operation(summary = "주변 애견 미용샵 조회", description = "Google Places에서 좌표 기준 최대 10km 내 애견 미용샵을 조회합니다.")
	public ApiResponse<NearbyPlacesResponse> findNearbyBeautyShops(
		@RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
		@RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
		@RequestParam(required = false) @Min(1) @Max(10000) Integer radiusMeters,
		@RequestParam(required = false) @Min(1) @Max(20) Integer limit,
		@RequestParam(required = false) String pageToken
	) {
		return ApiResponse.success(
			placeQueryService.findNearbyBeautyShops(latitude, longitude, radiusMeters, limit, pageToken)
		);
	}

	@GetMapping("/{googlePlaceId}")
	@Operation(summary = "애견 미용샵 상세 조회", description = "Google Place ID로 최신 장소 상세 정보를 조회합니다.")
	public ApiResponse<PlaceDetailResponse> findPlaceDetail(
		@Parameter(description = "Google Place ID") @PathVariable String googlePlaceId
	) {
		return ApiResponse.success(placeQueryService.findPlaceDetail(googlePlaceId));
	}

	@GetMapping("/photos")
	@Operation(summary = "장소 사진 URL 발급", description = "Google 사진 리소스 이름으로 단기 이미지 URL을 발급합니다.")
	public ApiResponse<PlacePhotoUriResponse> getPhotoUri(
		@RequestParam String photoName,
		@RequestParam(defaultValue = "800") @Min(1) @Max(1600) int maxWidthPx
	) {
		return ApiResponse.success(placeQueryService.getPhotoUri(photoName, maxWidthPx));
	}
}
