package com.singlemungle.backend.domain.beauty.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocalImageStorage implements ImageStorage {

	private static final Map<String, String> EXTENSIONS = Map.of(
		"image/jpeg", ".jpg",
		"image/png", ".png",
		"image/webp", ".webp",
		"text/plain", ".txt"
	);

	private final Path directory;
	private final String baseUrl;

	public LocalImageStorage(
		@Value("${app.image-storage.directory}") String directory,
		@Value("${app.image-storage.base-url}") String baseUrl
	) {
		this.directory = Path.of(directory);
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	@Override
	public String store(byte[] bytes, String mimeType) {
		try {
			Files.createDirectories(directory);
			String fileName = UUID.randomUUID() + extension(mimeType);
			Files.write(directory.resolve(fileName), bytes);
			return baseUrl + "/" + fileName;
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
		}
	}

	@Override
	public StoredImage read(String imageUrl) {
		Path path = resolveImageUrl(imageUrl);
		try {
			return new StoredImage(Files.readAllBytes(path), mimeType(path.getFileName().toString()));
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.STYLE_SIMULATION_NOT_FOUND);
		}
	}

	@Override
	public Resource load(String fileName) {
		Path path = directory.resolve(fileName).normalize();
		if (!path.startsWith(directory.normalize()) || !Files.exists(path)) {
			throw new BusinessException(ErrorCode.STYLE_SIMULATION_NOT_FOUND);
		}
		return new FileSystemResource(path);
	}

	@Override
	public void delete(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank() || !imageUrl.startsWith(baseUrl + "/")) {
			return;
		}

		String fileName = imageUrl.substring((baseUrl + "/").length());
		Path path = directory.resolve(fileName).normalize();
		if (!path.startsWith(directory.normalize())) {
			return;
		}

		try {
			Files.deleteIfExists(path);
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
		}
	}

	private String extension(String mimeType) {
		return EXTENSIONS.getOrDefault(mimeType, ".bin");
	}

	private Path resolveImageUrl(String imageUrl) {
		if (imageUrl == null || !imageUrl.startsWith(baseUrl + "/")) {
			throw new BusinessException(ErrorCode.STYLE_SIMULATION_NOT_FOUND);
		}
		String fileName = imageUrl.substring((baseUrl + "/").length());
		Path path = directory.resolve(fileName).normalize();
		if (!path.startsWith(directory.normalize()) || !Files.exists(path)) {
			throw new BusinessException(ErrorCode.STYLE_SIMULATION_NOT_FOUND);
		}
		return path;
	}

	private String mimeType(String fileName) {
		String lowerCaseFileName = fileName.toLowerCase();
		if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if (lowerCaseFileName.endsWith(".png")) {
			return "image/png";
		}
		if (lowerCaseFileName.endsWith(".webp")) {
			return "image/webp";
		}
		return "application/octet-stream";
	}
}
