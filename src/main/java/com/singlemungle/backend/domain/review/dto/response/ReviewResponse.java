package com.singlemungle.backend.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.singlemungle.backend.domain.review.entity.Review;

public record ReviewResponse(
	Long reviewId,
	Long userId,
	Long shopId,
	int rating,
	String content,
	String groomingStyleName,
	List<ReviewImageResponse> images,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static ReviewResponse from(Review review, List<ReviewImageResponse> images) {
		return new ReviewResponse(
			review.getId(),
			review.getUserId(),
			review.getPlaceId(),
			review.getRating(),
			review.getContent(),
			review.getGroomingStyleName(),
			images,
			review.getCreatedAt(),
			review.getUpdatedAt()
		);
	}
}
