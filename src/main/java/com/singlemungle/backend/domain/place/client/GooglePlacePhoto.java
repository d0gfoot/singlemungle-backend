package com.singlemungle.backend.domain.place.client;

import java.util.List;

public record GooglePlacePhoto(
	String name,
	List<String> authorAttributions
) {
}
