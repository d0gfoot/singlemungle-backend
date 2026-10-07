package com.singlemungle.backend.domain.place.dto;

import java.math.BigDecimal;

public record GooglePlaceReviewResponse(
	String reviewName,
	BigDecimal rating,
	String text,
	String originalText,
	String relativePublishTimeDescription,
	String publishTime,
	GooglePlaceReviewAuthorResponse author,
	String googleMapsUri,
	String flagContentUri
) {
}
