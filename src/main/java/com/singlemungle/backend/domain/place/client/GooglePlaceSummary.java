package com.singlemungle.backend.domain.place.client;

import java.math.BigDecimal;

public record GooglePlaceSummary(
	String googlePlaceId,
	String name,
	String formattedAddress,
	BigDecimal latitude,
	BigDecimal longitude,
	String googleMapsUri
) {
}
