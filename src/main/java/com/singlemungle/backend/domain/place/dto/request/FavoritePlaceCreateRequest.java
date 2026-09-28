package com.singlemungle.backend.domain.place.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FavoritePlaceCreateRequest(
	@NotBlank @Size(max = 255) String googlePlaceId,
	@Size(max = 500) String memo
) {
}
