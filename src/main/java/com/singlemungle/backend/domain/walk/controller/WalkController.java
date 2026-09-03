package com.singlemungle.backend.domain.walk.controller;

import java.util.List;
import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.auth.security.CustomUserPrincipal;
import com.singlemungle.backend.domain.walk.dto.request.WalkCreateRequest;
import com.singlemungle.backend.domain.walk.dto.response.MonthlyWalkSummaryResponse;
import com.singlemungle.backend.domain.walk.dto.response.WalkIdResponse;
import com.singlemungle.backend.domain.walk.dto.response.WalkListResponse;
import com.singlemungle.backend.domain.walk.dto.response.WalkResponse;
import com.singlemungle.backend.domain.walk.service.WalkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/care/pets/{petId}/walks")
@RequiredArgsConstructor
@Tag(name = "Walk", description = "반려동물 산책 기록 API")
@SecurityRequirement(name = "bearerAuth")
public class WalkController {

    private final WalkService walkService;

    /**
     * 최근 산책 기록
     */
    @GetMapping("/recent")
    @Operation(
            summary = "최근 산책 기록 조회",
            description = "현재 로그인 사용자의 반려동물 최근 산책 기록을 조회합니다."
    )
    public ApiResponse<List<WalkResponse>> getRecentWalks(
            @AuthenticationPrincipal CustomUserPrincipal principal,

            @Parameter(
                    description = "반려동물 ID",
                    example = "101"
            )
            @PathVariable Long petId,

            @Parameter(
                    description = "조회할 산책 기록 개수",
                    example = "5"
            )
            @RequestParam(defaultValue = "5") int limit
    ) {

        return ApiResponse.success(
                walkService.getRecentWalks(
                        principal.userId(),
                        petId,
                        limit
                )
        );
    }

    /**
     * 월간 통계
     */
    @GetMapping("/monthly-summary")
    @Operation(
            summary = "월간 산책 통계 조회",
            description = "이번 달 산책 횟수와 누적 거리, 시간, 칼로리를 조회합니다."
    )
    public ApiResponse<MonthlyWalkSummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal CustomUserPrincipal principal,

            @Parameter(
                    description = "반려동물 ID",
                    example = "101"
            )
            @PathVariable Long petId
    ) {

        return ApiResponse.success(
                walkService.getMonthlySummary(
                        principal.userId(),
                        petId
                )
        );
    }

    /**
     * 전체 산책 기록
     */
    @GetMapping
    @Operation(
            summary = "산책 기록 목록 조회",
            description = "현재 로그인 사용자의 반려동물 산책 기록을 최신순으로 조회합니다."
    )
    public ApiResponse<WalkListResponse> getWalks(
            @AuthenticationPrincipal CustomUserPrincipal principal,

            @Parameter(
                    description = "반려동물 ID",
                    example = "101"
            )
            @PathVariable Long petId,

            Pageable pageable
    ) {

        return ApiResponse.success(
                walkService.getWalks(
                        principal.userId(),
                        petId,
                        pageable
                )
        );
    }

    /**
     * 산책 기록 등록
     */
    @PostMapping
    @Operation(
            summary = "산책 기록 등록",
            description = "현재 로그인 사용자의 반려동물 산책 기록을 등록합니다."
    )
    public ApiResponse<WalkIdResponse> addWalk(
            @AuthenticationPrincipal CustomUserPrincipal principal,

            @Parameter(
                    description = "반려동물 ID",
                    example = "101"
            )
            @PathVariable Long petId,

            @Valid @RequestBody WalkCreateRequest request
    ) {

        Long walkId = walkService.addWalk(
                principal.userId(),
                petId,
                request
        );

        return ApiResponse.success(
                new WalkIdResponse(walkId)
        );
    }
}