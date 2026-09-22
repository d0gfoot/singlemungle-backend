package com.singlemungle.backend.domain.beauty.storage;

import org.springframework.core.io.Resource;

public interface ImageStorage {

	String store(byte[] bytes, String mimeType);

	StoredImage read(String imageUrl);

	Resource load(String fileName);

	void delete(String imageUrl);
}
