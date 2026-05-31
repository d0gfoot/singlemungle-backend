package com.singlemungle.backend.domain.place.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "place_images")
public class PlaceImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "place_id", nullable = false)
	private Place place;

	@Column(name = "image_url", nullable = false, columnDefinition = "text")
	private String imageUrl;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder = 0;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected PlaceImage() {
	}

	public PlaceImage(Place place, String imageUrl, int sortOrder) {
		this.place = place;
		this.imageUrl = imageUrl;
		this.sortOrder = sortOrder;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Place getPlace() {
		return place;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
