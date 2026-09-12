package com.singlemungle.backend.domain.beauty.storage;

import org.springframework.core.io.Resource;

public interface ImageStorage {

	String store(byte[] bytes, String mimeType);

	Resource load(String fileName);
}
