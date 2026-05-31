package com.singlemungle.backend.domain.place.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MockPlaceReviewService {

	private static final Map<String, ReviewStat> MOCK_REVIEW_STATS = Map.of(
		"12345", new ReviewStat(new BigDecimal("4.5"), 24),
		"67890", new ReviewStat(new BigDecimal("2.5"), 2),
		"mock-hospital-1", new ReviewStat(new BigDecimal("4.5"), 24),
		"mock-hospital-2", new ReviewStat(new BigDecimal("2.5"), 2)
	);

	public ReviewStat getReviewStat(String externalPlaceId) {
		return MOCK_REVIEW_STATS.getOrDefault(externalPlaceId, new ReviewStat(BigDecimal.ZERO, 0));
	}

	public record ReviewStat(
		BigDecimal ratingAverage,
		int reviewCount
	) {
	}
}
