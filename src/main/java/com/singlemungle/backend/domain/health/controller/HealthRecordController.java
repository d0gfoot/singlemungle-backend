package com.singlemungle.backend.domain.health.controller;

import java.util.List;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.health.dto.request.HealthRecordCreateRequest;
import com.singlemungle.backend.domain.health.dto.request.HealthRecordUpdateRequest;
import com.singlemungle.backend.domain.health.dto.response.HealthRecordDetailResponse;
import com.singlemungle.backend.domain.health.dto.response.HealthRecordIdResponse;
import com.singlemungle.backend.domain.health.dto.response.HealthRecordListResponse;
import com.singlemungle.backend.domain.health.service.HealthRecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/care/pets/{petId}/health-records")
@RequiredArgsConstructor
@Tag(name = "HealthRecord", description = "반려동물 건강 기록 API")
public class HealthRecordController {

    private final HealthRecordService healthService;
    private final Long mockUserId = 1L;

    @GetMapping
    @Operation(summary = "건강 기록 목록 조회", description = "현재 로그인 사용자가 소유한 반려동물의 건강 기록 목록을 조회합니다.")
    public ApiResponse<List<HealthRecordListResponse>> getHealthRecords(
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
            @Parameter(description = "건강 기록 유형", example = "VACCINE") @RequestParam(required = false) String recordType,
            Pageable pageable) {
        Page<HealthRecordListResponse> records = healthService.getHealthRecords(mockUserId, petId, recordType, pageable);
        return ApiResponse.success(records.getContent());
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "건강 기록 상세 조회", description = "현재 로그인 사용자가 소유한 반려동물의 건강 기록 상세 정보를 조회합니다.")
    public ApiResponse<HealthRecordDetailResponse> getHealthRecordDetail(
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
            @Parameter(description = "건강 기록 ID", example = "201") @PathVariable Long recordId) {
        HealthRecordDetailResponse detail = healthService.getHealthRecordDetail(mockUserId, petId, recordId);
        return ApiResponse.success(detail);
    }

    @PostMapping
    @Operation(summary = "건강 기록 등록", description = "현재 로그인 사용자가 소유한 반려동물에 건강 기록을 등록합니다.")
    public ApiResponse<HealthRecordIdResponse> addHealthRecord(
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
            @Valid @RequestBody HealthRecordCreateRequest request) {
        Long recordId = healthService.addHealthRecord(mockUserId, petId, request);
        return ApiResponse.success(new HealthRecordIdResponse(recordId));
    }

    @PatchMapping("/{recordId}")
    @Operation(summary = "건강 기록 수정", description = "현재 로그인 사용자가 소유한 반려동물의 건강 기록을 수정합니다.")
    public ApiResponse<Void> updateHealthRecord(
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
            @Parameter(description = "건강 기록 ID", example = "201") @PathVariable Long recordId,
            @Valid @RequestBody HealthRecordUpdateRequest request) {
        healthService.updateHealthRecord(mockUserId, petId, recordId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{recordId}")
    @Operation(summary = "건강 기록 삭제", description = "현재 로그인 사용자가 소유한 반려동물의 건강 기록을 삭제합니다.")
    public ApiResponse<Void> deleteHealthRecord(
            @Parameter(description = "반려동물 ID", example = "101") @PathVariable Long petId,
            @Parameter(description = "건강 기록 ID", example = "201") @PathVariable Long recordId) {
        healthService.deleteHealthRecord(mockUserId, petId, recordId);
        return ApiResponse.success();
    }
}
