package com.singlemungle.backend.domain.place.client;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.google-places.enabled", havingValue = "false", matchIfMissing = true)
public class FakeGooglePlacesClient implements GooglePlacesClient {

	public static final String PLACE_ID = "ChIJ-demo-beauty-shop";

	@Override
	public GooglePlaceSearchResult searchBeautyShops(
		BigDecimal latitude,
		BigDecimal longitude,
		int radiusMeters,
		int pageSize,
		String pageToken
	) {
		return new GooglePlaceSearchResult(List.of(new GooglePlaceSummary(
			PLACE_ID,
			"몽실몽실펫살롱",
			"서울 구로구 경인로 123",
			latitude,
			longitude,
			new BigDecimal("4.8"),
			25,
			new GooglePlacePhoto(
				"places/ChIJ-demo-beauty-shop/photos/demo-photo",
				List.of("테스트 제공자")
			),
			"https://maps.google.com/?cid=demo"
		)), null);
	}

	@Override
	public GooglePlaceSearchResult searchBeautyShopsByName(
		String query,
		int pageSize,
		String pageToken
	) {
		return searchBeautyShops(
			new BigDecimal("37.5000531"), new BigDecimal("126.8679927"), 10000, pageSize, pageToken
		);
	}

	@Override
	public GooglePlaceSearchResult searchNearbyBeautyShopsByName(
		String query,
		BigDecimal latitude,
		BigDecimal longitude,
		int radiusMeters,
		int pageSize,
		String pageToken
	) {
		return searchBeautyShops(latitude, longitude, radiusMeters, pageSize, pageToken);
	}

	@Override
	public GooglePlaceDetail getPlaceDetail(String googlePlaceId) {
		return new GooglePlaceDetail(
			googlePlaceId,
			"몽실몽실펫살롱",
			"서울 구로구 경인로 123",
			new BigDecimal("37.5000531"),
			new BigDecimal("126.8679927"),
			"02-1234-5678",
			"https://example.com",
			"https://maps.google.com/?cid=demo",
			true,
			new BigDecimal("4.8"),
			25,
			List.of("pet_care", "point_of_interest"),
			List.of("월요일: 오전 10:00~오후 8:00"),
			List.of(new GooglePlacePhoto(
				"places/ChIJ-demo-beauty-shop/photos/demo-photo",
				List.of("테스트 제공자")
			)),
			List.of(new GooglePlaceReview(
				"places/ChIJ-demo-beauty-shop/reviews/demo-review",
				new BigDecimal("5.0"),
				"친절하고 미용 결과가 만족스러웠어요.",
				"친절하고 미용 결과가 만족스러웠어요.",
				"1개월 전",
				"2026-08-28T09:00:00Z",
				new GooglePlaceReviewAuthor(
					"Google 리뷰 작성자",
					"https://www.google.com/maps/contrib/demo",
					"https://example.com/google-review-author.jpg"
				),
				"https://www.google.com/maps/reviews/demo",
				"https://www.google.com/local/review/rap/report"
			))
		);
	}

	@Override
	public String getPhotoUri(String photoName, int maxWidthPx) {
		return "https://example.com/google-place-photo.jpg";
	}
}
