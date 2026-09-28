package com.singlemungle.backend.domain.place.client;

import java.math.BigDecimal;

public record GooglePlaceReview(
	String name,
	BigDecimal rating,
	String text,
	String originalText,
	String relativePublishTimeDescription,
	String publishTime,
	GooglePlaceReviewAuthor author,
	String googleMapsUri,
	String flagContentUri
) {
}
