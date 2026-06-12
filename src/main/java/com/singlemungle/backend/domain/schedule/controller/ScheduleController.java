package com.singlemungle.backend.domain.schedule.controller;

import java.util.List;

import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.schedule.dto.request.ScheduleCreateRequest;
import com.singlemungle.backend.domain.schedule.dto.request.ScheduleUpdateRequest;
import com.singlemungle.backend.domain.schedule.dto.response.ScheduleIdResponse;
import com.singlemungle.backend.domain.schedule.dto.response.ScheduleResponse;
import com.singlemungle.backend.domain.schedule.service.ScheduleService;

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
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 관리 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "내 일정 목록 조회", description = "현재 로그인 사용자의 일정 목록을 조회합니다.")
    public ApiResponse<List<ScheduleResponse>> getSchedules() {
        Long userId = 1L;
        return ApiResponse.success(scheduleService.getSchedules(userId));
    }

    @PostMapping
    @Operation(summary = "일정 등록", description = "현재 로그인 사용자에게 새 일정을 등록합니다. petId가 있으면 본인 소유 반려동물인지 검증합니다.")
    public ApiResponse<ScheduleIdResponse> addSchedule(@Valid @RequestBody ScheduleCreateRequest request) {
        Long userId = 1L;
        Long scheduleId = scheduleService.addSchedule(userId, request);
        return ApiResponse.success(new ScheduleIdResponse(scheduleId));
    }

    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "현재 로그인 사용자가 소유한 일정을 수정합니다. petId를 변경하면 본인 소유 반려동물인지 검증합니다.")
    public ApiResponse<ScheduleIdResponse> updateSchedule(
            @Parameter(description = "일정 ID", example = "301") @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        Long userId = 1L;
        Long updatedId = scheduleService.updateSchedule(userId, scheduleId, request);
        return ApiResponse.success(new ScheduleIdResponse(updatedId));
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제", description = "현재 로그인 사용자가 소유한 일정을 삭제합니다.")
    public ApiResponse<Void> deleteSchedule(
            @Parameter(description = "일정 ID", example = "301") @PathVariable Long scheduleId) {
        Long userId = 1L;
        scheduleService.deleteSchedule(userId, scheduleId);
        return ApiResponse.success();
    }
}
