package com.singlemungle.backend.domain.place.dto;

import java.util.List;

public record NearbyPlacesResponse(
	List<NearbyPlaceItemResponse> places,
	String nextPageToken
) {
}
