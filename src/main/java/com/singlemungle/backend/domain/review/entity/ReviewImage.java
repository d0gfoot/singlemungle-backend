package com.singlemungle.backend.domain.review.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "review_images")
@SQLRestriction("deleted_at is null")
@Getter
public class ReviewImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "review_id", nullable = false)
	private Long reviewId;

	@Column(name = "image_url", nullable = false, columnDefinition = "text")
	private String imageUrl;

	@Column(name = "ai_reference_allowed", nullable = false)
	private boolean aiReferenceAllowed;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected ReviewImage() {
	}

	public ReviewImage(Long reviewId, String imageUrl, boolean aiReferenceAllowed) {
		this.reviewId = reviewId;
		this.imageUrl = imageUrl;
		this.aiReferenceAllowed = aiReferenceAllowed;
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}

	public void updateAiReferenceAllowed(boolean allowed) {
		aiReferenceAllowed = allowed;
	}

	public void delete() {
		deletedAt = LocalDateTime.now();
	}
}
