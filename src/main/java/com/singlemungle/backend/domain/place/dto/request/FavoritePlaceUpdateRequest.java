package com.singlemungle.backend.domain.place.dto.request;

import jakarta.validation.constraints.Size;

public record FavoritePlaceUpdateRequest(
	@Size(max = 500) String memo
) {
}
