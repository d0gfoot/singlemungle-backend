package com.singlemungle.backend.domain.place.dto.response;

import java.time.LocalDateTime;

import com.singlemungle.backend.domain.place.entity.FavoritePlace;

public record FavoritePlaceResponse(
	Long favoritePlaceId,
	String googlePlaceId,
	String memo,
	LocalDateTime createdAt
) {

	public static FavoritePlaceResponse from(FavoritePlace favoritePlace) {
		return new FavoritePlaceResponse(
			favoritePlace.getId(),
			favoritePlace.getGooglePlaceId(),
			favoritePlace.getMemo(),
			favoritePlace.getCreatedAt()
		);
	}
}
