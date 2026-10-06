package com.singlemungle.backend.domain.beauty.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.dto.response.GroomingStyleStatisticItemResponse;
import com.singlemungle.backend.domain.beauty.dto.response.GroomingStyleStatisticsResponse;
import com.singlemungle.backend.domain.beauty.entity.GroomingStylePreference;
import com.singlemungle.backend.domain.beauty.repository.GroomingStylePreferenceRepository;
import com.singlemungle.backend.domain.beauty.repository.GroomingStylePresetRepository;
import com.singlemungle.backend.domain.beauty.repository.StylePresetSelectionCount;
import com.singlemungle.backend.domain.pet.entity.Pet;
import com.singlemungle.backend.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroomingStyleStatisticsService {

	private final GroomingStylePreferenceRepository preferenceRepository;
	private final GroomingStylePresetRepository presetRepository;
	private final PetRepository petRepository;
	private final BreedNormalizer breedNormalizer;

	@Transactional
	public void recordSelection(Long userId, Pet pet, Long stylePresetId, Long simulationId) {
		if (stylePresetId == null) {
			return;
		}
		breedNormalizer.normalize(pet.getBreed()).ifPresent(breed -> {
			GroomingStylePreference preference = preferenceRepository
				.findByUserIdAndBreedCode(userId, breed.code())
				.orElseGet(() -> new GroomingStylePreference(
					userId, breed.code(), breed.name(), stylePresetId, simulationId
				));
			if (preference.getId() != null) {
				preference.update(breed.name(), stylePresetId, simulationId);
			}
			preferenceRepository.save(preference);
		});
	}

	@Transactional(readOnly = true)
	public GroomingStyleStatisticsResponse getStatistics(Long userId, Long petId) {
		Pet pet = petRepository.findByIdAndUserId(petId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
		BreedNormalizer.Breed breed = breedNormalizer.normalize(pet.getBreed())
			.orElseThrow(() -> new BusinessException(ErrorCode.PET_BREED_REQUIRED));
		Map<Long, StylePresetSelectionCount> counts = preferenceRepository
			.countSelectionsByBreedExcludingUser(breed.code(), userId).stream()
			.collect(Collectors.toMap(StylePresetSelectionCount::getStylePresetId, Function.identity()));
		long totalUsers = counts.values().stream().mapToLong(StylePresetSelectionCount::getSelectedUserCount).sum();

		var statistics = presetRepository.findByEnabledTrueOrderByIdAsc().stream()
			.map(preset -> {
				StylePresetSelectionCount count = counts.get(preset.getId());
				long selected = count == null ? 0 : count.getSelectedUserCount();
				BigDecimal percentage = totalUsers == 0
					? BigDecimal.ZERO.setScale(1)
					: BigDecimal.valueOf(selected * 100.0 / totalUsers).setScale(1, RoundingMode.HALF_UP);
				return new GroomingStyleStatisticItemResponse(
					preset.getId(), preset.getName(), preset.getDescription(), preset.getExampleImageUrl(), selected, percentage
				);
			})
			.toList();
		return new GroomingStyleStatisticsResponse(breed.code(), breed.name(), totalUsers, statistics);
	}
}
