package com.singlemungle.backend.domain.place.dto;

import java.util.List;

public record PlaceDetailResponse(
	Long placeId,
	String name,
	String phone,
	String todayOpenTime,
	String todayCloseTime,
	List<String> services,
	String homepageUrl,
	String reservationUrl,
	List<String> images,
	List<BusinessHourResponse> weeklyBusinessHours
) {
}
