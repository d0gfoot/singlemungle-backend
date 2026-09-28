package com.singlemungle.backend.domain.place.service;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.place.client.GooglePlaceDetail;
import com.singlemungle.backend.domain.place.client.GooglePlacesClient;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BeautyShopPlaceService {

	private final GooglePlacesClient googlePlacesClient;

	public GooglePlaceDetail getVerifiedBeautyShop(String googlePlaceId) {
		GooglePlaceDetail detail = googlePlacesClient.getPlaceDetail(normalize(googlePlaceId));
		if (!isBeautyShop(detail)) {
			throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
		}
		return detail;
	}

	public String normalize(String googlePlaceId) {
		if (googlePlaceId == null || googlePlaceId.isBlank() || googlePlaceId.length() > 255) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		return googlePlaceId.trim();
	}

	private boolean isBeautyShop(GooglePlaceDetail detail) {
		return detail.types() != null && detail.types().contains("pet_care");
	}
}
