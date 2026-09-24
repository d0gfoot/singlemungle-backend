package com.singlemungle.backend.domain.review.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "reviews")
@SQLRestriction("deleted_at is null")
@Getter
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "google_place_id", nullable = false, length = 255)
	private String googlePlaceId;

	@Column(nullable = false)
	private int rating;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "grooming_style_name", length = 100)
	private String groomingStyleName;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected Review() {
	}

	public Review(Long userId, String googlePlaceId, int rating, String content, String groomingStyleName) {
		this.userId = userId;
		this.googlePlaceId = googlePlaceId;
		this.rating = rating;
		this.content = content;
		this.groomingStyleName = groomingStyleName;
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public void update(int rating, String content, String groomingStyleName) {
		this.rating = rating;
		this.content = content;
		this.groomingStyleName = groomingStyleName;
	}

	public void delete() {
		deletedAt = LocalDateTime.now();
	}
}
