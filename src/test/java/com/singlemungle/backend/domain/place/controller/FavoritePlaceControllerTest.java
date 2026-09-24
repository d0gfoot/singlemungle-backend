package com.singlemungle.backend.domain.place.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.place.client.FakeGooglePlacesClient;
import com.singlemungle.backend.domain.place.entity.FavoritePlace;
import com.singlemungle.backend.domain.place.repository.FavoritePlaceRepository;
import com.singlemungle.backend.domain.user.entity.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {
	"/db/testdata/cleanup-care-test-data.sql",
	"/db/testdata/care-test-data.sql"
})
class FavoritePlaceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Autowired
	private FavoritePlaceRepository favoritePlaceRepository;

	@Test
	void createAndListFavoritePlace() throws Exception {
		mockMvc.perform(post("/api/v1/users/me/favorite-places")
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "googlePlaceId": "ChIJ-demo-beauty-shop",
					  "memo": "다음 미용 후보"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.googlePlaceId").value(FakeGooglePlacesClient.PLACE_ID))
			.andExpect(jsonPath("$.data.memo").value("다음 미용 후보"));

		mockMvc.perform(get("/api/v1/users/me/favorite-places")
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.content[0].googlePlaceId").value(FakeGooglePlacesClient.PLACE_ID));
	}

	@Test
	void createFavoritePlaceRejectsDuplicate() throws Exception {
		favoritePlaceRepository.save(new FavoritePlace(1L, FakeGooglePlacesClient.PLACE_ID, null));

		mockMvc.perform(post("/api/v1/users/me/favorite-places")
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"googlePlaceId": "ChIJ-demo-beauty-shop"}
					"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("FAVORITE_PLACE_ALREADY_EXISTS"));
	}

	@Test
	void updateAndDeleteFavoriteRequireOwner() throws Exception {
		FavoritePlace favorite = favoritePlaceRepository.save(
			new FavoritePlace(1L, FakeGooglePlacesClient.PLACE_ID, "기존 메모")
		);

		mockMvc.perform(patch("/api/v1/users/me/favorite-places/{favoritePlaceId}", favorite.getId())
				.header(HttpHeaders.AUTHORIZATION, bearerToken(2L))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"memo\": \"변경\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("FAVORITE_PLACE_NOT_FOUND"));

		mockMvc.perform(patch("/api/v1/users/me/favorite-places/{favoritePlaceId}", favorite.getId())
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"memo\": \"변경된 메모\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.memo").value("변경된 메모"));

		mockMvc.perform(delete("/api/v1/users/me/favorite-places/{favoritePlaceId}", favorite.getId())
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/users/me/favorite-places")
				.header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(0));
	}

	@Test
	void favoritePlaceRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/users/me/favorite-places"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
	}

	private String bearerToken(Long userId) {
		return "Bearer " + jwtTokenService.createAccessToken(userId, UserRole.USER);
	}
}
