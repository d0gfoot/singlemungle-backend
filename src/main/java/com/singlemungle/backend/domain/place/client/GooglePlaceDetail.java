package com.singlemungle.backend.domain.place.client;

import java.math.BigDecimal;
import java.util.List;

public record GooglePlaceDetail(
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
	List<String> types,
	List<String> weekdayDescriptions,
	List<GooglePlacePhoto> photos,
	List<GooglePlaceReview> reviews
) {
}
