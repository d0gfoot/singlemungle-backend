package com.singlemungle.backend.domain.place.client;

import java.util.List;

public record GooglePlaceSearchResult(
	List<GooglePlaceSummary> places,
	String nextPageToken
) {
}
