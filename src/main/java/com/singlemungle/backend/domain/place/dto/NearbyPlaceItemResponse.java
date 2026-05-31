package com.singlemungle.backend.domain.place.dto;

import java.math.BigDecimal;

import com.singlemungle.backend.domain.place.entity.PlaceType;

public record NearbyPlaceItemResponse(
	Long placeId,
	String externalPlaceId,
	PlaceType placeType,
	String name,
	BigDecimal latitude,
	BigDecimal longitude,
	long distanceMeters,
	boolean isOpen,
	BigDecimal ratingAverage,
	int reviewCount
) {
}
