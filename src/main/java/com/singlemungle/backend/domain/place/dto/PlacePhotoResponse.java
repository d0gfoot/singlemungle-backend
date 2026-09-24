package com.singlemungle.backend.domain.place.dto;

import java.util.List;

public record PlacePhotoResponse(
	String photoName,
	List<String> authorAttributions
) {
}
