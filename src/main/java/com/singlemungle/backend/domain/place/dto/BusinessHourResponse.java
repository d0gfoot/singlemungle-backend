package com.singlemungle.backend.domain.place.dto;

public record BusinessHourResponse(
	String dayOfWeek,
	String openTime,
	String closeTime
) {
}
