package com.singlemungle.backend.domain.place.client;

import java.math.BigDecimal;

public interface GooglePlacesClient {

	GooglePlaceSearchResult searchBeautyShops(
		BigDecimal latitude,
		BigDecimal longitude,
		int radiusMeters,
		int pageSize,
		String pageToken
	);

	GooglePlaceSearchResult searchBeautyShopsByName(
		String query,
		BigDecimal latitude,
		BigDecimal longitude,
		int radiusMeters,
		int pageSize,
		String pageToken
	);

	GooglePlaceDetail getPlaceDetail(String googlePlaceId);

	String getPhotoUri(String photoName, int maxWidthPx);
}
