package com.singlemungle.backend.domain.place.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlaceDetailResponse(
	String googlePlaceId,
	String name,
	String formattedAddress,
	BigDecimal latitude,
	BigDecimal longitude,
	String phone,
	String websiteUri,
	String googleMapsUri,
	Boolean openNow,
	BigDecimal rating,
	Integer userRatingCount,
	List<String> weekdayDescriptions,
	List<PlacePhotoResponse> photos
) {
}
