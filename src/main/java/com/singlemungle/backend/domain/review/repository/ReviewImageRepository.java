package com.singlemungle.backend.domain.review.repository;

import java.util.List;

import com.singlemungle.backend.domain.review.entity.ReviewImage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

	List<ReviewImage> findByReviewIdOrderByIdAsc(Long reviewId);
}
