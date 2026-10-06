package com.singlemungle.backend.domain.beauty.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Table(
	name = "grooming_style_preferences",
	uniqueConstraints = @UniqueConstraint(name = "uk_style_preferences_user_breed", columnNames = {"user_id", "breed_code"})
)
@Getter
public class GroomingStylePreference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "breed_code", nullable = false, length = 100)
	private String breedCode;

	@Column(name = "breed_name", nullable = false, length = 100)
	private String breedName;

	@Column(name = "style_preset_id", nullable = false)
	private Long stylePresetId;

	@Column(name = "source_simulation_id", nullable = false)
	private Long sourceSimulationId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected GroomingStylePreference() {
	}

	public GroomingStylePreference(
		Long userId,
		String breedCode,
		String breedName,
		Long stylePresetId,
		Long sourceSimulationId
	) {
		this.userId = userId;
		this.breedCode = breedCode;
		this.breedName = breedName;
		this.stylePresetId = stylePresetId;
		this.sourceSimulationId = sourceSimulationId;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = this.createdAt;
	}

	public void update(String breedName, Long stylePresetId, Long sourceSimulationId) {
		this.breedName = breedName;
		this.stylePresetId = stylePresetId;
		this.sourceSimulationId = sourceSimulationId;
		this.updatedAt = LocalDateTime.now();
	}
}
