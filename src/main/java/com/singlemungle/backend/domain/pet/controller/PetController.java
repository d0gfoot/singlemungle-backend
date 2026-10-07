package com.singlemungle.backend.domain.pet.controller;

import java.util.List;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.auth.security.CustomUserPrincipal;
import com.singlemungle.backend.domain.pet.dto.request.PetCreateRequest;
import com.singlemungle.backend.domain.pet.dto.request.PetUpdateRequest;
import com.singlemungle.backend.domain.pet.dto.response.PetIdResponse;
import com.singlemungle.backend.domain.pet.dto.response.PetResponse;
import com.singlemungle.backend.domain.pet.dto.response.PetProfileImageResponse;
import com.singlemungle.backend.domain.pet.service.PetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
@Tag(name = "Pet", description = "반려동물 프로필 API")
@SecurityRequirement(name = "bearerAuth")
public class PetController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "내 반려동물 목록 조회", description = "현재 로그인 사용자의 반려동물 프로필 목록을 조회합니다.")
    public ApiResponse<List<PetResponse>> getMyPets(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(petService.getMyPets(principal.userId()));
    }

    @PostMapping
    @Operation(summary = "반려동물 등록", description = "현재 로그인 사용자에게 새 반려동물 프로필을 등록합니다.")
    public ApiResponse<PetIdResponse> addPet(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody PetCreateRequest req) {
        Long petId = petService.addPet(principal.userId(), req);
        return ApiResponse.success(new PetIdResponse(petId));
    }

    @PatchMapping("/{petId}")
    @Operation(summary = "반려동물 정보 수정", description = "현재 로그인 사용자가 소유한 반려동물 프로필을 수정합니다.")
    public ApiResponse<Void> updatePet(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
            @Valid @RequestBody PetUpdateRequest req) {
        petService.updatePet(principal.userId(), petId, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{petId}")
    @Operation(summary = "반려동물 삭제", description = "현재 로그인 사용자가 소유한 반려동물 프로필을 soft delete 처리합니다.")
    public ApiResponse<Void> deletePet(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId) {
        petService.deletePet(principal.userId(), petId);
        return ApiResponse.success();
    }

	@PutMapping(value = "/{petId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "반려동물 프로필 이미지 등록·교체", description = "JPEG, PNG, WebP 이미지를 최대 10MB까지 업로드합니다.")
	public ApiResponse<PetProfileImageResponse> uploadProfileImage(
			@AuthenticationPrincipal CustomUserPrincipal principal,
			@Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
			@RequestPart("image") MultipartFile image) {
		String imageUrl = petService.uploadProfileImage(principal.userId(), petId, image);
		return ApiResponse.success(new PetProfileImageResponse(imageUrl));
	}

	@DeleteMapping("/{petId}/profile-image")
	@Operation(summary = "반려동물 프로필 이미지 삭제", description = "저장된 프로필 이미지를 삭제하고 이미지 URL을 초기화합니다.")
	public ApiResponse<Void> deleteProfileImage(
			@AuthenticationPrincipal CustomUserPrincipal principal,
			@Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId) {
		petService.deleteProfileImage(principal.userId(), petId);
		return ApiResponse.success();
	}

    @PatchMapping("/{petId}/representative")
    @Operation(summary = "대표 반려동물 설정", description = "현재 로그인 사용자의 대표 반려동물을 변경합니다.")
    public ApiResponse<Void> setRepresentative(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "대표로 설정할 반려동물 ID", example = "101") @PathVariable Long petId) {
        petService.setRepresentative(principal.userId(), petId);
        return ApiResponse.success();
    }
}
