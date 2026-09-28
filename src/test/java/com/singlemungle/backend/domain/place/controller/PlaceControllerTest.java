package com.singlemungle.backend.domain.place.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singlemungle.backend.domain.place.client.FakeGooglePlacesClient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlaceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void findNearbyPlacesReturnsOnlyGoogleBeautyShops() throws Exception {
		mockMvc.perform(get("/api/v1/places/nearby")
				.param("latitude", "37.5000530744517")
				.param("longitude", "126.86799268384159")
				.param("radiusMeters", "5000"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.places.length()").value(1))
			.andExpect(jsonPath("$.data.places[0].googlePlaceId").value(FakeGooglePlacesClient.PLACE_ID))
			.andExpect(jsonPath("$.data.places[0].name").value("몽실몽실펫살롱"))
			.andExpect(jsonPath("$.data.places[0].distanceMeters").value(greaterThanOrEqualTo(0)))
			.andExpect(jsonPath("$.data.nextPageToken").doesNotExist());
	}

	@Test
	void findNearbyPlacesUsesConfiguredLocationWhenCoordinatesAreOmitted() throws Exception {
		mockMvc.perform(get("/api/v1/places/nearby"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.places[0].latitude").value(37.5000530744517))
			.andExpect(jsonPath("$.data.places[0].longitude").value(126.86799268384159));
	}

	@Test
	void findNearbyPlacesRejectsRadiusOverTenKilometers() throws Exception {
		mockMvc.perform(get("/api/v1/places/nearby").param("radiusMeters", "10001"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void findPlaceDetailReturnsGooglePlaceData() throws Exception {
		mockMvc.perform(get("/api/v1/places/{googlePlaceId}", FakeGooglePlacesClient.PLACE_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.googlePlaceId").value(FakeGooglePlacesClient.PLACE_ID))
			.andExpect(jsonPath("$.data.name").value("몽실몽실펫살롱"))
			.andExpect(jsonPath("$.data.phone").value("02-1234-5678"))
			.andExpect(jsonPath("$.data.openNow").value(true))
			.andExpect(jsonPath("$.data.rating").value(4.8))
			.andExpect(jsonPath("$.data.userRatingCount").value(25))
			.andExpect(jsonPath("$.data.photos[0].photoName")
				.value("places/ChIJ-demo-beauty-shop/photos/demo-photo"))
			.andExpect(jsonPath("$.data.googleReviews[0].rating").value(5.0))
			.andExpect(jsonPath("$.data.googleReviews[0].text").value("친절하고 미용 결과가 만족스러웠어요."))
			.andExpect(jsonPath("$.data.googleReviews[0].author.displayName").value("Google 리뷰 작성자"))
			.andExpect(jsonPath("$.data.googleReviews[0].googleMapsUri")
				.value("https://www.google.com/maps/reviews/demo"));
	}

	@Test
	void getPhotoUriReturnsShortLivedGooglePhotoUrl() throws Exception {
		mockMvc.perform(get("/api/v1/places/photos")
				.param("photoName", "places/ChIJ-demo-beauty-shop/photos/demo-photo")
				.param("maxWidthPx", "800"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.photoUri").value("https://example.com/google-place-photo.jpg"));
	}

	@Test
	void getPhotoUriRejectsInvalidPhotoName() throws Exception {
		mockMvc.perform(get("/api/v1/places/photos")
				.param("photoName", "https://malicious.example/image.jpg"))
			.andExpect(status().isBadRequest());
	}
}
