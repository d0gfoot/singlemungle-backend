package com.singlemungle.backend.common.image;

import com.singlemungle.backend.domain.beauty.storage.ImageStorage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "저장 이미지 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class ImageController {

	private final ImageStorage imageStorage;

	@GetMapping("/{fileName}")
	@Operation(
		summary = "저장 이미지 조회",
		description = "서버에 저장된 이미지를 조회합니다.",
		parameters = @Parameter(name = "fileName", in = ParameterIn.PATH, schema = @Schema(type = "string"))
	)
	public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
		return ResponseEntity.ok()
			.contentType(imageMediaType(fileName))
			.header(
				"Content-Disposition",
				ContentDisposition.inline().filename(fileName).build().toString()
			)
			.body(imageStorage.load(fileName));
	}

	private MediaType imageMediaType(String fileName) {
		String lowerCaseFileName = fileName.toLowerCase();
		if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
			return MediaType.IMAGE_JPEG;
		}
		if (lowerCaseFileName.endsWith(".png")) {
			return MediaType.IMAGE_PNG;
		}
		if (lowerCaseFileName.endsWith(".webp")) {
			return MediaType.parseMediaType("image/webp");
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}
}
