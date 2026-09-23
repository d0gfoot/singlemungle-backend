package com.singlemungle.backend.domain.review.dto.response;

import com.singlemungle.backend.domain.review.entity.ReviewImage;

public record ReviewImageResponse(
	Long reviewImageId,
	String imageUrl,
	boolean aiReferenceAllowed
) {

	public static ReviewImageResponse from(ReviewImage image) {
		return new ReviewImageResponse(image.getId(), image.getImageUrl(), image.isAiReferenceAllowed());
	}
}
