package com.singlemungle.backend.domain.user.controller;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.auth.security.CustomUserPrincipal;
import com.singlemungle.backend.domain.user.dto.request.UserUpdateRequest;
import com.singlemungle.backend.domain.user.service.UserService;
import com.singlemungle.backend.domain.user.dto.response.UserInfoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "User", description = "마이페이지 사용자 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "내 정보 조회", description = "현재 로그인 사용자의 정보를 조회합니다.")
    public ApiResponse<UserInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UserInfoResponse response = userService.getUserInfo(principal.userId());
        return ApiResponse.success(response);
    }

    @PatchMapping
    @Operation(summary = "내 정보 수정", description = "현재 로그인 사용자의 닉네임과 전화번호를 수정합니다.")
    public ApiResponse<Void> updateMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UserUpdateRequest req) {
        userService.updateUserInfo(principal.userId(), req.nickname(), req.phone());
        return ApiResponse.success();
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "현재 로그인 사용자를 soft delete 처리합니다.")
    public ApiResponse<Void> deleteMyAccount(@AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.deleteUser(principal.userId());
        return ApiResponse.success();
    }
}
