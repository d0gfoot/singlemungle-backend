package com.singlemungle.backend.domain.beauty.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalTemporaryImageStorage implements TemporaryImageStorage {

	private static final Map<String, String> EXTENSIONS = Map.of(
		"image/jpeg", ".jpg",
		"image/png", ".png",
		"image/webp", ".webp"
	);

	private final Path directory;

	public LocalTemporaryImageStorage(@Value("${app.image-storage.temp-directory}") String directory) {
		this.directory = Path.of(directory);
	}

	@Override
	public TemporaryImage store(byte[] bytes, String mimeType) {
		try {
			Files.createDirectories(directory);
			String fileName = UUID.randomUUID() + EXTENSIONS.getOrDefault(mimeType, ".bin");
			Files.write(resolve(fileName), bytes);
			return new TemporaryImage(fileName, mimeType);
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
		}
	}

	@Override
	public byte[] read(TemporaryImage image) {
		try {
			return Files.readAllBytes(resolve(image.fileName()));
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
		}
	}

	@Override
	public void delete(TemporaryImage image) {
		if (image == null) {
			return;
		}
		try {
			Files.deleteIfExists(resolve(image.fileName()));
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
		}
	}

	@Override
	public int deleteOlderThan(LocalDateTime threshold) {
		if (!Files.exists(directory)) {
			return 0;
		}
		FileTime cutoff = FileTime.from(threshold.atZone(ZoneId.systemDefault()).toInstant());
		AtomicInteger deletedCount = new AtomicInteger();
		try (Stream<Path> files = Files.list(directory)) {
			files.filter(Files::isRegularFile).forEach(path -> deleteIfOlder(path, cutoff, deletedCount));
			return deletedCount.get();
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED);
		}
	}

	private void deleteIfOlder(Path path, FileTime cutoff, AtomicInteger deletedCount) {
		try {
			if (Files.getLastModifiedTime(path).compareTo(cutoff) < 0 && Files.deleteIfExists(path)) {
				deletedCount.incrementAndGet();
			}
		} catch (IOException ignored) {
			// A later cleanup run retries files that could not be removed.
		}
	}

	private Path resolve(String fileName) {
		Path path = directory.resolve(fileName).normalize();
		if (!path.startsWith(directory.normalize())) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		return path;
	}
}
