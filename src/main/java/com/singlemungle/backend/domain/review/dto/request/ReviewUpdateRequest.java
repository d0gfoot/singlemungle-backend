package com.singlemungle.backend.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(
	@Min(1) @Max(5) int rating,
	@NotBlank @Size(max = 2000) String content,
	@Size(max = 100) String groomingStyleName,
	boolean aiReferenceAllowed
) {
}
