package com.singlemungle.backend.domain.place.service;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.place.dto.request.FavoritePlaceCreateRequest;
import com.singlemungle.backend.domain.place.dto.request.FavoritePlaceUpdateRequest;
import com.singlemungle.backend.domain.place.dto.response.FavoritePlaceResponse;
import com.singlemungle.backend.domain.place.entity.FavoritePlace;
import com.singlemungle.backend.domain.place.repository.FavoritePlaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoritePlaceService {

	private final FavoritePlaceRepository favoritePlaceRepository;
	private final BeautyShopPlaceService beautyShopPlaceService;

	@Transactional
	public FavoritePlaceResponse create(Long userId, FavoritePlaceCreateRequest request) {
		String googlePlaceId = beautyShopPlaceService
			.getVerifiedBeautyShop(request.googlePlaceId())
			.googlePlaceId();
		if (favoritePlaceRepository.existsByUserIdAndGooglePlaceId(userId, googlePlaceId)) {
			throw new BusinessException(ErrorCode.FAVORITE_PLACE_ALREADY_EXISTS);
		}
		FavoritePlace favoritePlace = favoritePlaceRepository.save(
			new FavoritePlace(userId, googlePlaceId, normalizeMemo(request.memo()))
		);
		return FavoritePlaceResponse.from(favoritePlace);
	}

	@Transactional(readOnly = true)
	public PageResponse<FavoritePlaceResponse> getMyFavorites(Long userId, Pageable pageable) {
		return PageResponse.from(favoritePlaceRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
			.map(FavoritePlaceResponse::from));
	}

	@Transactional
	public FavoritePlaceResponse update(Long userId, Long favoritePlaceId, FavoritePlaceUpdateRequest request) {
		FavoritePlace favoritePlace = findOwnedFavorite(userId, favoritePlaceId);
		favoritePlace.updateMemo(normalizeMemo(request.memo()));
		return FavoritePlaceResponse.from(favoritePlace);
	}

	@Transactional
	public void delete(Long userId, Long favoritePlaceId) {
		favoritePlaceRepository.delete(findOwnedFavorite(userId, favoritePlaceId));
	}

	private FavoritePlace findOwnedFavorite(Long userId, Long favoritePlaceId) {
		return favoritePlaceRepository.findByIdAndUserId(favoritePlaceId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_PLACE_NOT_FOUND));
	}

	private String normalizeMemo(String memo) {
		return memo == null || memo.isBlank() ? null : memo.trim();
	}
}
