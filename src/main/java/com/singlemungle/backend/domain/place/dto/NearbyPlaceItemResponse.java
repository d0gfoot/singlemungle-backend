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
	long distanceMeters,
	String googleMapsUri
) {
}
