package com.singlemungle.backend.domain.beauty.storage;

public record StoredImage(
	byte[] bytes,
	String mimeType
) {
}
