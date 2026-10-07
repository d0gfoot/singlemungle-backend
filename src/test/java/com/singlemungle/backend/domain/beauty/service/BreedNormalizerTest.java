package com.singlemungle.backend.domain.beauty.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BreedNormalizerTest {

	private final BreedNormalizer normalizer = new BreedNormalizer();

	@Test
	void normalizesKoreanAndEnglishAliasesToSameBreed() {
		assertThat(normalizer.normalize("푸들").orElseThrow().code()).isEqualTo("POODLE");
		assertThat(normalizer.normalize(" Poodle ").orElseThrow().code()).isEqualTo("POODLE");
	}

	@Test
	void keepsUnknownBreedAsStableNormalizedValue() {
		assertThat(normalizer.normalize("믹스 견").orElseThrow().code()).isEqualTo("믹스_견");
	}

	@Test
	void returnsEmptyForMissingBreed() {
		assertThat(normalizer.normalize(" ")).isEmpty();
	}
}
