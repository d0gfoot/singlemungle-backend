package com.singlemungle.backend.domain.beauty.storage;

import java.time.LocalDateTime;

public interface TemporaryImageStorage {

	TemporaryImage store(byte[] bytes, String mimeType);

	byte[] read(TemporaryImage image);

	void delete(TemporaryImage image);

	int deleteOlderThan(LocalDateTime threshold);
}
