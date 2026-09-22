package com.singlemungle.backend.domain.beauty.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "beauty_style_simulations")
@SQLRestriction("deleted_at is null")
@Getter
public class BeautyStyleSimulation {

	private static final int RETENTION_DAYS = 30;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "pet_id", nullable = false)
	private Long petId;

	@Column(name = "shop_id", nullable = false)
	private Long shopId;

	@Column(name = "style_keyword", nullable = false, length = 100)
	private String styleKeyword;

	@Column(name = "additional_request", length = 500)
	private String additionalRequest;

	@Column(name = "source_image_url", columnDefinition = "text")
	private String sourceImageUrl;

	@Column(name = "style_image_url", columnDefinition = "text")
	private String styleImageUrl;

	@Column(name = "generated_image_url", columnDefinition = "text")
	private String generatedImageUrl;

	@Column(name = "storage_provider", nullable = false, length = 30)
	private String storageProvider;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BeautyStyleSimulationStatus status = BeautyStyleSimulationStatus.PENDING;

	@Column(name = "failure_reason", columnDefinition = "text")
	private String failureReason;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected BeautyStyleSimulation() {
	}

	public BeautyStyleSimulation(
		Long userId,
		Long petId,
		Long shopId,
		String styleKeyword,
		String additionalRequest,
		String sourceImageUrl,
		String styleImageUrl,
		String storageProvider
	) {
		this.userId = userId;
		this.petId = petId;
		this.shopId = shopId;
		this.styleKeyword = styleKeyword;
		this.additionalRequest = additionalRequest;
		this.sourceImageUrl = sourceImageUrl;
		this.styleImageUrl = styleImageUrl;
		this.storageProvider = storageProvider;
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		if (this.expiresAt == null) {
			this.expiresAt = now.plusDays(RETENTION_DAYS);
		}
	}

	public boolean complete(String generatedImageUrl) {
		if (status != BeautyStyleSimulationStatus.PENDING) {
			return false;
		}
		this.generatedImageUrl = generatedImageUrl;
		this.status = BeautyStyleSimulationStatus.COMPLETED;
		this.failureReason = null;
		return true;
	}

	public boolean fail(String failureReason) {
		if (status != BeautyStyleSimulationStatus.PENDING) {
			return false;
		}
		this.status = BeautyStyleSimulationStatus.FAILED;
		this.failureReason = failureReason;
		this.sourceImageUrl = null;
		this.styleImageUrl = null;
		this.generatedImageUrl = null;
		return true;
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}
}
