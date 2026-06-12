package com.singlemungle.backend.domain.pet.controller;

import java.util.List;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.pet.dto.request.PetCreateRequest;
import com.singlemungle.backend.domain.pet.dto.request.PetUpdateRequest;
import com.singlemungle.backend.domain.pet.dto.response.PetIdResponse;
import com.singlemungle.backend.domain.pet.dto.response.PetResponse;
import com.singlemungle.backend.domain.pet.service.PetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
@Tag(name = "Pet", description = "반려동물 프로필 API")
public class PetController {

    private final PetService petService;
    private final Long mockUserId = 1L; // 임시 유저 ID

    @GetMapping
    @Operation(summary = "내 반려동물 목록 조회", description = "현재 로그인 사용자의 반려동물 프로필 목록을 조회합니다.")
    public ApiResponse<List<PetResponse>> getMyPets() {
        return ApiResponse.success(petService.getMyPets(mockUserId));
    }

    @PostMapping
    @Operation(summary = "반려동물 등록", description = "현재 로그인 사용자에게 새 반려동물 프로필을 등록합니다.")
    public ApiResponse<PetIdResponse> addPet(@Valid @RequestBody PetCreateRequest req) {
        Long petId = petService.addPet(mockUserId, req);
        return ApiResponse.success(new PetIdResponse(petId));
    }

    @PatchMapping("/{petId}")
    @Operation(summary = "반려동물 정보 수정", description = "현재 로그인 사용자가 소유한 반려동물 프로필을 수정합니다.")
    public ApiResponse<Void> updatePet(
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
            @Valid @RequestBody PetUpdateRequest req) {
        petService.updatePet(mockUserId, petId, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{petId}")
    @Operation(summary = "반려동물 삭제", description = "현재 로그인 사용자가 소유한 반려동물 프로필을 soft delete 처리합니다.")
    public ApiResponse<Void> deletePet(
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId) {
        petService.deletePet(mockUserId, petId);
        return ApiResponse.success();
    }

    @PatchMapping("/{petId}/representative")
    @Operation(summary = "대표 반려동물 설정", description = "현재 로그인 사용자의 대표 반려동물을 변경합니다.")
    public ApiResponse<Void> setRepresentative(
            @Parameter(description = "대표로 설정할 반려동물 ID", example = "101") @PathVariable Long petId) {
        petService.setRepresentative(mockUserId, petId);
        return ApiResponse.success();
    }
}
