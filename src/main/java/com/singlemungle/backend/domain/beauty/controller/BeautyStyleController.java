package com.singlemungle.backend.domain.beauty.controller;

import java.util.List;
import com.singlemungle.backend.common.response.ApiResponse;
import com.singlemungle.backend.domain.beauty.dto.response.BeautyStyleResponse;
import com.singlemungle.backend.domain.beauty.service.BeautyStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/beauty-shops/{shopId}/styles")
@RequiredArgsConstructor
@Tag(name = "BeautyStyle", description = "미용샵 스타일 API")
@SecurityRequirement(name = "bearerAuth")
public class BeautyStyleController {

    private final BeautyStyleService beautyStyleService;

    @GetMapping
    @Operation(
            summary = "미용 스타일 목록 조회",
            description = "선택한 미용샵에서 제공하는 미용 스타일 목록을 조회합니다."
    )
    public ApiResponse<List<BeautyStyleResponse>> getStyles(
            @Parameter(description = "미용샵 ID", example = "1")
            @PathVariable Long shopId
    ) {
        return ApiResponse.success(
                beautyStyleService.getStyles(shopId)
        );
    }
}