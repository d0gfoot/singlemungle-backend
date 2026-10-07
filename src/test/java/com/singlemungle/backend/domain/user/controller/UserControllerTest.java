package com.singlemungle.backend.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
	"/db/testdata/care-test-data.sql"
})
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Test
	void getMyInfoReturnsCurrentUser() throws Exception {
		mockMvc.perform(get("/api/v1/users/me")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.userId").value(1))
			.andExpect(jsonPath("$.data.nickname").value("보호자1"))
			.andExpect(jsonPath("$.data.phone").value("010-1111-1111"))
			.andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/users/1.jpg"));
	}

	@Test
	void updateMyInfoUpdatesNicknameAndPhone() throws Exception {
		mockMvc.perform(patch("/api/v1/users/me")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "nickname": "수정보호자",
					  "phone": "010-9999-9999",
					  "profileImageUrl": "https://example.com/users/updated.jpg"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/users/me")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nickname").value("수정보호자"))
			.andExpect(jsonPath("$.data.phone").value("010-9999-9999"))
			.andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/users/updated.jpg"));
	}

	@Test
	void deleteMyAccountSoftDeletesUser() throws Exception {
		mockMvc.perform(delete("/api/v1/users/me")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"));

		mockMvc.perform(get("/api/v1/users/me")
				.header(HttpHeaders.AUTHORIZATION, bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
	}

	private String bearerToken() {
		return "Bearer " + jwtTokenService.createAccessToken(1L, UserRole.USER);
	}
}
