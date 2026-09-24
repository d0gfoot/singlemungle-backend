package com.singlemungle.backend.domain.beauty.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "grooming_style_presets")
@Getter
public class GroomingStylePreset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "example_image_url", columnDefinition = "text")
	private String exampleImageUrl;

	@Column(name = "prompt_description", nullable = false, columnDefinition = "text")
	private String promptDescription;

	@Column(name = "negative_prompt", columnDefinition = "text")
	private String negativePrompt;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected GroomingStylePreset() {
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
