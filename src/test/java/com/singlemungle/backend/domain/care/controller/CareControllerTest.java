package com.singlemungle.backend.domain.care.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

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
			.andExpect(jsonPath("$.data[0].name").value("초코"))
			.andExpect(jsonPath("$.data[0].profileImageUrl").doesNotExist());
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
	void addPetCreatesPetForCurrentUser() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "몽이",
					  "gender": "FEMALE",
					  "breed": "포메라니안",
					  "birthDate": "2022-03-04",
					  "weight": 2.8,
					  "profileImageUrl": "https://example.com/pets/mongi.jpg",
					  "memo": "작고 활발함"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.petId").isNumber())
			.andReturn();

		Integer petId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.petId");

		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[?(@.petId == %d)].name", petId).value(contains("몽이")))
			.andExpect(jsonPath("$.data[?(@.petId == %d)].breed", petId).value(contains("포메라니안")))
			.andExpect(jsonPath("$.data[?(@.petId == %d)].profileImageUrl", petId)
				.value(contains("https://example.com/pets/mongi.jpg")));
	}

	@Test
	void updatePetChangesPetProfile() throws Exception {
		mockMvc.perform(patch("/api/v1/pets/{petId}", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "초코수정",
					  "weight": 4.9,
					  "profileImageUrl": "https://example.com/pets/choco-updated.jpg",
					  "memo": "체중 관리 중"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].petId").value(101))
			.andExpect(jsonPath("$.data[0].name").value("초코수정"))
			.andExpect(jsonPath("$.data[0].profileImageUrl")
				.value("https://example.com/pets/choco-updated.jpg"));
	}

	@Test
	void updatePetRejectsAnotherUsersPet() throws Exception {
		mockMvc.perform(patch("/api/v1/pets/{petId}", 102)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "변경 불가"
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void uploadProfileImageStoresImageForOwnedPet() throws Exception {
		MockMultipartFile image = new MockMultipartFile(
			"image", "choco.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
		MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/pets/{petId}/profile-image", 101)
			.file(image);
		request.with(httpRequest -> {
			httpRequest.setMethod("PUT");
			return httpRequest;
		});

		MvcResult result = mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.profileImageUrl").isString())
			.andReturn();

		String imageUrl = JsonPath.read(result.getResponse().getContentAsString(), "$.data.profileImageUrl");
		mockMvc.perform(get(imageUrl)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(resultMatcher -> org.junit.jupiter.api.Assertions.assertArrayEquals(
				new byte[] {1, 2, 3}, resultMatcher.getResponse().getContentAsByteArray()));

		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].profileImageUrl").value(imageUrl));
	}

	@Test
	void uploadProfileImageRejectsAnotherUsersPet() throws Exception {
		MockMultipartFile image = new MockMultipartFile(
			"image", "latte.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
		MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/pets/{petId}/profile-image", 102)
			.file(image);
		request.with(httpRequest -> {
			httpRequest.setMethod("PUT");
			return httpRequest;
		});

		mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void uploadProfileImageRejectsUnsupportedContentType() throws Exception {
		MockMultipartFile image = new MockMultipartFile(
			"image", "choco.gif", MediaType.IMAGE_GIF_VALUE, new byte[] {1, 2, 3});
		MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/pets/{petId}/profile-image", 101)
			.file(image);
		request.with(httpRequest -> {
			httpRequest.setMethod("PUT");
			return httpRequest;
		});

		mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void deleteProfileImageClearsOwnedPetImage() throws Exception {
		MockMultipartFile image = new MockMultipartFile(
			"image", "choco.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
		MockMultipartHttpServletRequestBuilder uploadRequest = multipart(
			"/api/v1/pets/{petId}/profile-image", 101).file(image);
		uploadRequest.with(httpRequest -> {
			httpRequest.setMethod("PUT");
			return httpRequest;
		});
		MvcResult uploadResult = mockMvc.perform(uploadRequest.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andReturn();
		String imageUrl = JsonPath.read(uploadResult.getResponse().getContentAsString(), "$.data.profileImageUrl");

		mockMvc.perform(delete("/api/v1/pets/{petId}/profile-image", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].profileImageUrl").doesNotExist());

		mockMvc.perform(get(imageUrl)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound());
	}

	@Test
	void deleteProfileImageRejectsAnotherUsersPet() throws Exception {
		mockMvc.perform(delete("/api/v1/pets/{petId}/profile-image", 102)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
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
	void setRepresentativeChangesRepresentativePet() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "몽이",
					  "breed": "포메라니안"
					}
					"""))
			.andExpect(status().isOk())
			.andReturn();

		Integer petId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.petId");

		mockMvc.perform(patch("/api/v1/pets/{petId}/representative", petId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/pets")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[?(@.petId == 101)].isRepresentative").value(contains(false)))
			.andExpect(jsonPath("$.data[?(@.petId == %d)].isRepresentative", petId).value(contains(true)));
	}

	@Test
	void getHealthRecordsReturnsOwnedPetRecords() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].recordId").value(201))
			.andExpect(jsonPath("$.data[0].recordType").value("VACCINE"))
			.andExpect(jsonPath("$.data[0].title").value("종합백신"));
	}

	@Test
	void getHealthRecordsRequiresOwnedPet() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records", 102)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PET_NOT_FOUND"));
	}

	@Test
	void getHealthRecordDetailReturnsRecord() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 201)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.recordId").value(201))
			.andExpect(jsonPath("$.data.hospitalName").value("사랑동물병원"))
			.andExpect(jsonPath("$.data.description").value("종합백신 접종"));
	}

	@Test
	void getHealthRecordDetailRequiresMatchingPetId() throws Exception {
		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 202)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("HEALTH_RECORD_NOT_FOUND"));
	}

	@Test
	void addHealthRecordCreatesRecordForOwnedPet() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/care/pets/{petId}/health-records", 101)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "recordType": "HOSPITAL",
					  "title": "정기 검진",
					  "hospitalName": "사랑동물병원",
					  "recordDate": "2026-06-10",
					  "description": "검진 결과 이상 없음",
					  "nextScheduleDate": "2026-12-10"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.recordId").isNumber())
			.andReturn();

		Integer recordId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.recordId");

		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, recordId)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.recordType").value("HOSPITAL"))
			.andExpect(jsonPath("$.data.title").value("정기 검진"));
	}

	@Test
	void updateHealthRecordChangesRecord() throws Exception {
		mockMvc.perform(patch("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 201)
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "recordType": "HOSPITAL",
					  "title": "검진 완료",
					  "hospitalName": "튼튼동물병원",
					  "recordDate": "2026-06-03",
					  "description": "검진 기록 수정"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 201)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.recordType").value("HOSPITAL"))
			.andExpect(jsonPath("$.data.title").value("검진 완료"))
			.andExpect(jsonPath("$.data.hospitalName").value("튼튼동물병원"));
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
	void deleteHealthRecordRemovesRecord() throws Exception {
		mockMvc.perform(delete("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 201)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/care/pets/{petId}/health-records/{recordId}", 101, 201)
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
	void getSchedulesReturnsCurrentUserSchedules() throws Exception {
		mockMvc.perform(get("/api/v1/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].scheduleId").value(301))
			.andExpect(jsonPath("$.data[0].title").value("정기 검진"));
	}

	@Test
	void addScheduleCreatesSchedule() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "petId": 101,
					  "scheduleType": "BEAUTY",
					  "title": "미용 예약",
					  "startAt": "2026-08-01T13:00:00",
					  "endAt": "2026-08-01T14:00:00",
					  "location": "몽실몽실펫살롱",
					  "memo": "곰돌이컷",
					  "isAlarm": true
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.scheduleId").isNumber())
			.andReturn();

		Integer scheduleId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.scheduleId");

		mockMvc.perform(get("/api/v1/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[?(@.scheduleId == %d)].scheduleType", scheduleId).value(contains("BEAUTY")))
			.andExpect(jsonPath("$.data[?(@.scheduleId == %d)].title", scheduleId).value(contains("미용 예약")));
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
	void deleteScheduleRemovesSchedule() throws Exception {
		mockMvc.perform(delete("/api/v1/schedules/{scheduleId}", 301)
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));
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
