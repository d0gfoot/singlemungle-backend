package com.singlemungle.backend.domain.beauty.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class BreedNormalizer {

	private static final Map<String, Breed> KNOWN_BREEDS = Map.ofEntries(
		Map.entry("포메", new Breed("POMERANIAN", "포메라니안")),
		Map.entry("포메라니안", new Breed("POMERANIAN", "포메라니안")),
		Map.entry("pomeranian", new Breed("POMERANIAN", "포메라니안")),
		Map.entry("말티", new Breed("MALTESE", "말티즈")),
		Map.entry("말티즈", new Breed("MALTESE", "말티즈")),
		Map.entry("maltese", new Breed("MALTESE", "말티즈")),
		Map.entry("푸들", new Breed("POODLE", "푸들")),
		Map.entry("poodle", new Breed("POODLE", "푸들")),
		Map.entry("비숑", new Breed("BICHON_FRISE", "비숑 프리제")),
		Map.entry("비숑프리제", new Breed("BICHON_FRISE", "비숑 프리제")),
		Map.entry("bichonfrise", new Breed("BICHON_FRISE", "비숑 프리제")),
		Map.entry("시츄", new Breed("SHIH_TZU", "시츄")),
		Map.entry("shihtzu", new Breed("SHIH_TZU", "시츄")),
		Map.entry("치와와", new Breed("CHIHUAHUA", "치와와")),
		Map.entry("chihuahua", new Breed("CHIHUAHUA", "치와와")),
		Map.entry("요크셔테리어", new Breed("YORKSHIRE_TERRIER", "요크셔 테리어")),
		Map.entry("yorkshireterrier", new Breed("YORKSHIRE_TERRIER", "요크셔 테리어")),
		Map.entry("골든리트리버", new Breed("GOLDEN_RETRIEVER", "골든 리트리버")),
		Map.entry("goldenretriever", new Breed("GOLDEN_RETRIEVER", "골든 리트리버"))
	);

	public Optional<Breed> normalize(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}
		String displayName = Normalizer.normalize(value.trim(), Normalizer.Form.NFKC);
		String lookupKey = displayName.toLowerCase(Locale.ROOT).replaceAll("[\\s_-]", "");
		Breed knownBreed = KNOWN_BREEDS.get(lookupKey);
		if (knownBreed != null) {
			return Optional.of(knownBreed);
		}
		String code = displayName.toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
		return Optional.of(new Breed(code.substring(0, Math.min(code.length(), 100)), displayName));
	}

	public record Breed(String code, String name) {
	}
}
