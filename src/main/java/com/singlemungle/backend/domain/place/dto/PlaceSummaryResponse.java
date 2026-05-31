package com.singlemungle.backend.domain.place.dto;

import java.math.BigDecimal;

public record PlaceSummaryResponse(
	String externalPlaceId,
	Long placeId,
	String name,
	BigDecimal latitude,
	BigDecimal longitude,
	BigDecimal ratingAverage,
	int reviewCount,
	boolean isOpen,
	String dayOfWeek,
	String openTime,
	String closeTime
) {
}
