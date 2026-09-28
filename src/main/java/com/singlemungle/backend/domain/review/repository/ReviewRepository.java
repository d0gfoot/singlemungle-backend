package com.singlemungle.backend.domain.review.repository;

import java.util.Optional;

import com.singlemungle.backend.domain.review.entity.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findByGooglePlaceIdOrderByCreatedAtDesc(String googlePlaceId, Pageable pageable);

	Optional<Review> findByIdAndGooglePlaceId(Long id, String googlePlaceId);

	Optional<Review> findByIdAndGooglePlaceIdAndUserId(Long id, String googlePlaceId, Long userId);
}
