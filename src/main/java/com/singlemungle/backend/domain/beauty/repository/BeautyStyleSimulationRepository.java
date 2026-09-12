package com.singlemungle.backend.domain.beauty.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeautyStyleSimulationRepository extends JpaRepository<BeautyStyleSimulation, Long> {

	Optional<BeautyStyleSimulation> findByIdAndUserId(Long id, Long userId);

	Page<BeautyStyleSimulation> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
		Long userId,
		LocalDateTime now,
		Pageable pageable
	);
}
