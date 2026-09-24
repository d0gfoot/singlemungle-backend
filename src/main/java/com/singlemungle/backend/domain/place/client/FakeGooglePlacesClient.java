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
			"https://maps.google.com/?cid=demo"
		)), null);
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
			))
		);
	}

	@Override
	public String getPhotoUri(String photoName, int maxWidthPx) {
		return "https://example.com/google-place-photo.jpg";
	}
}
