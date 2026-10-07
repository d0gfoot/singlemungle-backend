package com.singlemungle.backend.domain.place.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record NearbyPlaceItemResponse(
	@Schema(description = "Google Place ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4")
	String googlePlaceId,
	String name,
	String formattedAddress,
	BigDecimal latitude,
	BigDecimal longitude,
	@Schema(description = "Google 평균 평점. 평점이 없으면 null", example = "4.7")
	BigDecimal rating,
	@Schema(description = "Google 평가 수", example = "128")
	Integer userRatingCount,
	@Schema(description = "대표 사진. 사진이 없으면 null")
	PlacePhotoResponse thumbnail,
	@Schema(description = "검색 기준점과의 거리. 전국 검색에서는 null", example = "320")
	Long distanceMeters,
	String googleMapsUri
) {
}
