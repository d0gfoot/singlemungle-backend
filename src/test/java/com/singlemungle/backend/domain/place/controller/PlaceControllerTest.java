package com.singlemungle.backend.domain.place.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {
	"/db/testdata/cleanup-place-test-data.sql",
	"/db/testdata/place-test-data.sql"
})
class PlaceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void findNearbyPlacesReturnsHospitalsWhenHospitalTypeRequested() throws Exception {
		mockMvc.perform(get("/api/v1/places/nearby")
				.param("latitude", "37.4980000")
				.param("longitude", "127.0276000")
				.param("radiusMeters", "1000")
				.param("placeTypes", "HOSPITAL"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.message").value("요청 성공"))
			.andExpect(jsonPath("$.data.places.length()").value(1))
			.andExpect(jsonPath("$.data.places[0].placeId").isNumber())
			.andExpect(jsonPath("$.data.places[0].placeType").value("HOSPITAL"))
			.andExpect(jsonPath("$.data.places[0].name").value("사랑동물병원"))
			.andExpect(jsonPath("$.data.places[0].distanceMeters").value(greaterThanOrEqualTo(0)))
			.andExpect(jsonPath("$.data.places[0].isOpen").value(true))
			.andExpect(jsonPath("$.data.places[0].ratingAverage").value(4.5));
	}

	@Test
	void findNearbyPlacesReturnsBeautyPlacesWhenBeautyTypeRequested() throws Exception {
		mockMvc.perform(get("/api/v1/places/nearby")
				.param("latitude", "37.4980000")
				.param("longitude", "127.0276000")
				.param("radiusMeters", "1000")
				.param("placeTypes", "BEAUTY"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.places.length()").value(1))
			.andExpect(jsonPath("$.data.places[0].placeType").value("BEAUTY"))
			.andExpect(jsonPath("$.data.places[0].name").value("몽실몽실펫살롱"));
	}

	@Test
	void findNearbyPlacesReturnsHospitalsAndBeautyPlacesByDistanceWhenTypesOmitted() throws Exception {
		mockMvc.perform(get("/api/v1/places/nearby")
				.param("latitude", "37.4980000")
				.param("longitude", "127.0276000")
				.param("radiusMeters", "2000")
				.param("limit", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.places.length()").value(3))
			.andExpect(jsonPath("$.data.places[0].externalPlaceId").value("12345"))
			.andExpect(jsonPath("$.data.places[0].placeType").value("HOSPITAL"))
			.andExpect(jsonPath("$.data.places[0].name").value("사랑동물병원"))
			.andExpect(jsonPath("$.data.places[0].distanceMeters").value(greaterThanOrEqualTo(0)))
			.andExpect(jsonPath("$.data.places[0].isOpen").value(true))
			.andExpect(jsonPath("$.data.places[0].ratingAverage").value(4.5))
			.andExpect(jsonPath("$.data.places[0].reviewCount").value(24))
			.andExpect(jsonPath("$.data.places[1].placeType").value("BEAUTY"))
			.andExpect(jsonPath("$.data.places[1].name").value("몽실몽실펫살롱"));
	}

	@Test
	void findPlaceSummariesReturnsRequestedExternalPlaceOrder() throws Exception {
		mockMvc.perform(get("/api/v1/places/summary")
				.param("provider", "KAKAO")
				.param("externalPlaceIds", "12345", "67890"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].externalPlaceId").value("12345"))
			.andExpect(jsonPath("$.data[0].name").value("사랑동물병원"))
			.andExpect(jsonPath("$.data[0].latitude").value(37.4983000))
			.andExpect(jsonPath("$.data[0].longitude").value(127.0276000))
			.andExpect(jsonPath("$.data[0].ratingAverage").value(4.5))
			.andExpect(jsonPath("$.data[0].reviewCount").value(24))
			.andExpect(jsonPath("$.data[0].isOpen").value(true))
			.andExpect(jsonPath("$.data[0].dayOfWeek").value("MONDAY"))
			.andExpect(jsonPath("$.data[0].openTime").value("09:00"))
			.andExpect(jsonPath("$.data[0].closeTime").value("18:00"))
			.andExpect(jsonPath("$.data[1].externalPlaceId").value("67890"))
			.andExpect(jsonPath("$.data[1].name").value("튼튼동물병원"))
			.andExpect(jsonPath("$.data[1].ratingAverage").value(2.5))
			.andExpect(jsonPath("$.data[1].reviewCount").value(2));
	}

	@Test
	void findPlaceDetailReturnsBusinessHoursServicesAndImages() throws Exception {
		mockMvc.perform(get("/api/v1/places/summary")
				.param("provider", "KAKAO")
				.param("externalPlaceIds", "12345"))
			.andExpect(status().isOk())
			.andDo(result -> {
				String placeId = com.jayway.jsonpath.JsonPath.read(
					result.getResponse().getContentAsString(),
					"$.data[0].placeId"
				).toString();

				mockMvc.perform(get("/api/v1/places/{placeId}", placeId))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.code").value("SUCCESS"))
					.andExpect(jsonPath("$.data.placeId").value(Long.parseLong(placeId)))
					.andExpect(jsonPath("$.data.name").value("사랑동물병원"))
					.andExpect(jsonPath("$.data.phone").value("02-1111-2222"))
					.andExpect(jsonPath("$.data.todayOpenTime").value("09:00"))
					.andExpect(jsonPath("$.data.todayCloseTime").value("18:00"))
					.andExpect(jsonPath("$.data.services[0]").value("일반 진료"))
					.andExpect(jsonPath("$.data.services[1]").value("예방접종"))
					.andExpect(jsonPath("$.data.homepageUrl").value("https://place.example.com/hospital-1"))
					.andExpect(jsonPath("$.data.reservationUrl").value("https://reservation.example.com/hospital-1"))
					.andExpect(jsonPath("$.data.images.length()").value(2))
					.andExpect(jsonPath("$.data.weeklyBusinessHours.length()").value(7))
					.andExpect(jsonPath("$.data.weeklyBusinessHours[0].dayOfWeek").value("MONDAY"));
			});
	}

	@TestConfiguration
	static class FixedClockConfig {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(
				Instant.parse("2026-06-01T01:00:00Z"),
				ZoneId.of("Asia/Seoul")
			);
		}
	}
}
