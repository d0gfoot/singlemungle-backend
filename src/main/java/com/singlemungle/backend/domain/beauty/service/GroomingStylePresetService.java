package com.singlemungle.backend.domain.beauty.service;

import java.util.List;

import com.singlemungle.backend.domain.beauty.dto.response.GroomingStylePresetResponse;
import com.singlemungle.backend.domain.beauty.repository.GroomingStylePresetRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroomingStylePresetService {

	private final GroomingStylePresetRepository presetRepository;

	public List<GroomingStylePresetResponse> getPresets() {
		return presetRepository.findByEnabledTrueOrderByIdAsc().stream()
			.map(GroomingStylePresetResponse::from)
			.toList();
	}
}
