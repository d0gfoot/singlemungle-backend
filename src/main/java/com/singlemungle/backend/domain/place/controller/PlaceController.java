package com.singlemungle.backend.domain.place.controller;

import java.math.BigDecimal;
import java.util.List;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.place.dto.NearbyPlacesResponse;
import com.singlemungle.backend.domain.place.dto.PlaceDetailResponse;
import com.singlemungle.backend.domain.place.dto.PlaceSummaryResponse;
import com.singlemungle.backend.domain.place.entity.PlaceProvider;
import com.singlemungle.backend.domain.place.entity.PlaceType;
import com.singlemungle.backend.domain.place.service.PlaceQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/places")
@Tag(name = "Place", description = "장소 API")
public class PlaceController {

	private final PlaceQueryService placeQueryService;

	public PlaceController(PlaceQueryService placeQueryService) {
		this.placeQueryService = placeQueryService;
	}

	@GetMapping("/summary")
	@Operation(summary = "장소 간단 정보 조회", description = "외부 장소 ID 목록으로 캐시된 장소의 간단한 정보를 조회합니다.")
	public ApiResponse<List<PlaceSummaryResponse>> findPlaceSummaries(
		@RequestParam PlaceProvider provider,
		@RequestParam List<String> externalPlaceIds
	) {
		return ApiResponse.success(placeQueryService.findPlaceSummaries(provider, externalPlaceIds));
	}

	@GetMapping("/{placeId}")
	@Operation(summary = "장소 상세 조회", description = "캐시된 장소의 상세 정보, 운영시간, 서비스, 예약 URL을 조회합니다.")
	public ApiResponse<PlaceDetailResponse> findPlaceDetail(@PathVariable Long placeId) {
		return ApiResponse.success(placeQueryService.findPlaceDetail(placeId));
	}

	@GetMapping("/nearby")
	@Operation(
		summary = "주변 장소 조회",
		description = "현재 좌표 기준 반경 내 장소를 타입별로 필터링하여 거리순으로 조회합니다. placeTypes를 생략하면 병원과 미용샵을 함께 조회합니다."
	)
	public ApiResponse<NearbyPlacesResponse> findNearbyPlaces(
		@RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
		@RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
		@RequestParam(required = false) @Positive Integer radiusMeters,
		@RequestParam(required = false) List<PlaceType> placeTypes,
		@RequestParam(required = false) @Positive Integer limit
	) {
		return ApiResponse.success(
			placeQueryService.findNearbyPlaces(latitude, longitude, radiusMeters, placeTypes, limit)
		);
	}
}
