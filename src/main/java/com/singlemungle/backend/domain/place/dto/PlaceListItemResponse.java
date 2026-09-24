package com.singlemungle.backend.domain.place.dto;

import java.math.BigDecimal;

import com.singlemungle.backend.domain.place.entity.PlaceType;

public record PlaceListItemResponse(
	Long placeId,
	PlaceType placeType,
	String name,
	String address,
	String roadAddress,
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
