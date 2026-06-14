package com.singlemungle.backend.domain.care.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.singlemungle.backend.domain.auth.service.JwtTokenService;
import com.singlemungle.backend.domain.user.entity.UserRole;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {
	"/db/testdata/cleanup-care-test-data.sql",
	"/db/testdata/care-test-data.sql"
})
class CareControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Test
	void getMyPetsReturnsOnlyMockUserPets() throws Exception {
		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].petId").value(101))
			.andExpect(jsonPath("$.data[0].name").value("초코"));
	}

	@Test
	void addPetValidatesRequiredName() throws Exception {
		mockMvc.perform(post("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "breed": "푸들"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void deletePetReturnsNotFoundWhenPetBelongsToAnotherUser() throws Exception {
		mockMvc.perform(delete("/api/v1/pets/{petId}", 102)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void deletePetReturnsSuccessWhenPetBelongsToMockUser() throws Exception {
		mockMvc.perform(delete("/api/v1/pets/{petId}", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));
	}

	@Test
	void deletePetSoftDeletesPetFromMyPetList() throws Exception {
		mockMvc.perform(delete("/api/v1/pets/{petId}", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	void getHealthRecordsRequiresOwnedPet() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records", 102)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void getHealthRecordDetailRequiresMatchingPetId() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 202)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("HEALTH_RECORD_NOT_FOUND"));
	}

	@Test
	void updateHealthRecordRequiresMatchingPetId() throws Exception {
		mockMvc.perform(patch("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 202)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": "수정 시도"
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("HEALTH_RECORD_NOT_FOUND"));
	}

	@Test
	void deleteHealthRecordRequiresMatchingPetId() throws Exception {
		mockMvc.perform(delete("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 202)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("HEALTH_RECORD_NOT_FOUND"));
	}

	@Test
	void addHealthRecordValidatesRequiredFields() throws Exception {
		mockMvc.perform(post("/api/v1/care/pets/{petId}/health-records", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "recordType": "VACCINE"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void addHealthRecordRequiresOwnedPet() throws Exception {
		mockMvc.perform(post("/api/v1/care/pets/{petId}/health-records", 102)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "recordType": "VACCINE",
					  "title": "종합백신",
					  "recordDate": "2026-06-01"
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void deleteScheduleReturnsNotFoundWhenScheduleBelongsToAnotherUser() throws Exception {
		mockMvc.perform(delete("/api/v1/schedules/{scheduleId}", 302)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
	}

	@Test
	void updateScheduleReturnsSuccessWhenScheduleBelongsToMockUser() throws Exception {
		mockMvc.perform(patch("/api/v1/schedules/{scheduleId}", 301)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": "정기 검진 변경"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.scheduleId").value(301));
	}

	@Test
	void addScheduleValidatesRequiredFields() throws Exception {
		mockMvc.perform(post("/api/v1/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": "일정"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void addScheduleRequiresOwnedPetWhenPetIdProvided() throws Exception {
		mockMvc.perform(post("/api/v1/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "petId": 102,
					  "scheduleType": "HOSPITAL",
					  "title": "정기 검진",
					  "startAt": "2026-07-01T10:00:00"
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void updateScheduleRequiresOwnedPetWhenPetIdProvided() throws Exception {
		mockMvc.perform(patch("/api/v1/schedules/{scheduleId}", 301)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "petId": 102
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	private String bearerToken() {
		return "Bearer " + jwtTokenService.createAccessToken(1L, UserRole.USER);
	}
}
