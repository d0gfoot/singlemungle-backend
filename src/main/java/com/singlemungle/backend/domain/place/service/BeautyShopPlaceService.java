package com.singlemungle.backend.domain.place.service;

import java.util.List;
import java.util.Locale;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.place.client.GooglePlaceDetail;
import com.singlemungle.backend.domain.place.client.GooglePlacesClient;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BeautyShopPlaceService {

	private static final List<String> BEAUTY_SHOP_NAME_KEYWORDS = List.of(
		"미용", "애견", "반려견", "펫", "살롱", "groom", "salon"
	);

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
		boolean petCare = detail.types() != null && detail.types().contains("pet_care");
		String normalizedName = detail.name() == null ? "" : detail.name().toLowerCase(Locale.ROOT);
		boolean beautyName = BEAUTY_SHOP_NAME_KEYWORDS.stream().anyMatch(normalizedName::contains);
		return petCare && beautyName;
	}
}
