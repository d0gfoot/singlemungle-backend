package com.singlemungle.backend.domain.review.repository;

import java.util.Optional;

import com.singlemungle.backend.domain.review.entity.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findByPlaceIdOrderByCreatedAtDesc(Long placeId, Pageable pageable);

	Optional<Review> findByIdAndPlaceId(Long id, Long placeId);

	Optional<Review> findByIdAndPlaceIdAndUserId(Long id, Long placeId, Long userId);
}
