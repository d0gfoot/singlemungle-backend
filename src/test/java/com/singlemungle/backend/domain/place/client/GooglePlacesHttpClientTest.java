package com.singlemungle.backend.domain.place.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.singlemungle.backend.common.config.GooglePlacesProperties;

import org.junit.jupiter.api.Test;

class GooglePlacesHttpClientTest {

	private final GooglePlacesHttpClient client = new GooglePlacesHttpClient(new GooglePlacesProperties());

	@Test
	void parsesGoogleReviewAndAuthorAttribution() {
		Map<String, Object> review = Map.ofEntries(
			Map.entry("name", "places/place-id/reviews/review-id"),
			Map.entry("rating", 4.0),
			Map.entry("text", Map.of("text", "번역된 리뷰", "languageCode", "ko")),
			Map.entry("originalText", Map.of("text", "Original review", "languageCode", "en")),
			Map.entry("relativePublishTimeDescription", "2개월 전"),
			Map.entry("publishTime", "2026-07-28T09:00:00Z"),
			Map.entry("authorAttribution", Map.of(
				"displayName", "Reviewer",
				"uri", "https://www.google.com/maps/contrib/reviewer",
				"photoUri", "https://example.com/reviewer.jpg"
			)),
			Map.entry("googleMapsUri", "https://www.google.com/maps/reviews/review-id"),
			Map.entry("flagContentUri", "https://www.google.com/local/review/rap/report")
		);
		Map<String, Object> place = Map.ofEntries(
			Map.entry("id", "place-id"),
			Map.entry("displayName", Map.of("text", "그루밍솔루션")),
			Map.entry("types", List.of("pet_care")),
			Map.entry("reviews", List.of(review))
		);

		GooglePlaceDetail detail = client.toDetail(place);

		assertThat(detail.reviews()).hasSize(1);
		GooglePlaceReview parsedReview = detail.reviews().getFirst();
		assertThat(parsedReview.text()).isEqualTo("번역된 리뷰");
		assertThat(parsedReview.originalText()).isEqualTo("Original review");
		assertThat(parsedReview.author().displayName()).isEqualTo("Reviewer");
		assertThat(parsedReview.author().uri()).isEqualTo("https://www.google.com/maps/contrib/reviewer");
		assertThat(parsedReview.googleMapsUri()).isEqualTo("https://www.google.com/maps/reviews/review-id");
		assertThat(parsedReview.flagContentUri()).isNotBlank();
	}

	@Test
	void returnsEmptyReviewListWhenGoogleDoesNotProvideReviews() {
		GooglePlaceDetail detail = client.toDetail(Map.of(
			"id", "place-id",
			"displayName", Map.of("text", "리뷰 없는 미용샵"),
			"types", List.of("pet_care")
		));

		assertThat(detail.reviews()).isEmpty();
	}

	@Test
	void parsesFirstSearchPhotoAsThumbnail() {
		GooglePlaceSummary summary = client.toSummary(Map.of(
			"id", "place-id",
			"displayName", Map.of("text", "사진 있는 미용샵"),
			"photos", List.of(
				Map.of(
					"name", "places/place-id/photos/first-photo",
					"authorAttributions", List.of(Map.of("displayName", "첫 번째 제공자"))
				),
				Map.of("name", "places/place-id/photos/second-photo")
			)
		));

		assertThat(summary.thumbnailPhoto()).isNotNull();
		assertThat(summary.thumbnailPhoto().name()).isEqualTo("places/place-id/photos/first-photo");
		assertThat(summary.thumbnailPhoto().authorAttributions()).containsExactly("첫 번째 제공자");
	}

	@Test
	void returnsNullThumbnailWhenSearchHasNoPhoto() {
		GooglePlaceSummary summary = client.toSummary(Map.of(
			"id", "place-id",
			"displayName", Map.of("text", "사진 없는 미용샵")
		));

		assertThat(summary.thumbnailPhoto()).isNull();
	}
}
