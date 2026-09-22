package com.singlemungle.backend.domain.beauty.repository;

import java.util.List;
import java.util.Optional;

import com.singlemungle.backend.domain.beauty.entity.GroomingStylePreset;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroomingStylePresetRepository extends JpaRepository<GroomingStylePreset, Long> {

	List<GroomingStylePreset> findByEnabledTrueOrderByIdAsc();

	Optional<GroomingStylePreset> findByIdAndEnabledTrue(Long id);
}
