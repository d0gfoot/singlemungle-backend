package com.singlemungle.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "내 정보 수정 요청")
public record UserUpdateRequest(
	@Schema(description = "닉네임", example = "멍글보호자")
	@Size(max = 50) String nickname,
	@Schema(description = "전화번호", example = "010-1234-5678")
	@Size(max = 30) String phone
) {
}
