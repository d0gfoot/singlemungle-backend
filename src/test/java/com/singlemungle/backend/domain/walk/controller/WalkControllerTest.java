package com.singlemungle.backend.domain.walk.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singlemungle.backend.domain.auth.service.JwtTokenService;
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
	"/db/testdata/walk-test-data.sql"
})
class WalkControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Test
	void getWalksReturnsOwnedPetWalks() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/walks", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.content.length()").value(2))
			.andExpect(jsonPath("$.data.content[0].walkId").value(502));
	}

	@Test
	void getRecentWalksHonorsLimit() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/walks/recent", 101)
				.param("limit", "1")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].walkId").value(502));
	}

	@Test
	void getMonthlySummaryReturnsAggregate() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/walks/monthly-summary", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.walkCount").value(2))
			.andExpect(jsonPath("$.data.totalDistanceKm").value(3.7))
			.andExpect(jsonPath("$.data.totalDurationMinutes").value(50));
	}

	@Test
	void addWalkCreatesWalkForOwnedPet() throws Exception {
		mockMvc.perform(post("/api/v1/care/pets/{petId}/walks", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "startedAt": "2026-09-03T09:00:00",
					  "endedAt": "2026-09-03T09:40:00",
					  "distanceKm": 3.0,
					  "durationMinutes": 40,
					  "routeData": [
					    {
					      "latitude": 37.4983,
					      "longitude": 127.0276
					    }
					  ],
					  "memo": "새 산책"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.walkId").isNumber());
	}

	@Test
	void addWalkRequiresOwnedPet() throws Exception {
		mockMvc.perform(post("/api/v1/care/pets/{petId}/walks", 102)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "startedAt": "2026-09-03T09:00:00",
					  "endedAt": "2026-09-03T09:40:00",
					  "distanceKm": 3.0,
					  "durationMinutes": 40
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void addWalkValidatesRequiredFields() throws Exception {
		mockMvc.perform(post("/api/v1/care/pets/{petId}/walks", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "distanceKm": 3.0
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	private String bearerToken() {
		return "Bearer " + jwtTokenService.createAccessToken(1L, UserRole.USER);
	}
}
