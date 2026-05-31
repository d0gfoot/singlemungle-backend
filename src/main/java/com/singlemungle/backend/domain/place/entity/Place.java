package com.singlemungle.backend.domain.place.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "places",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_places_provider_external_place_id",
			columnNames = {"provider", "external_place_id"}
		)
	}
)
public class Place {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PlaceProvider provider;

	@Column(name = "external_place_id", nullable = false, length = 100)
	private String externalPlaceId;

	@Enumerated(EnumType.STRING)
	@Column(name = "place_type", nullable = false, length = 30)
	private PlaceType placeType;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 255)
	private String address;

	@Column(name = "road_address", length = 255)
	private String roadAddress;

	@Column(nullable = false, precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(nullable = false, precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(length = 30)
	private String phone;

	@Column(name = "place_url", columnDefinition = "text")
	private String placeUrl;

	@Column(name = "reservation_url", columnDefinition = "text")
	private String reservationUrl;

	@Column(name = "is_verified", nullable = false)
	private boolean verified = false;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Place() {
	}

	public Place(
		PlaceProvider provider,
		String externalPlaceId,
		PlaceType placeType,
		String name,
		String address,
		String roadAddress,
		BigDecimal latitude,
		BigDecimal longitude,
		String phone,
		String placeUrl,
		String reservationUrl,
		boolean verified
	) {
		this.provider = provider;
		this.externalPlaceId = externalPlaceId;
		this.placeType = placeType;
		this.name = name;
		this.address = address;
		this.roadAddress = roadAddress;
		this.latitude = latitude;
		this.longitude = longitude;
		this.phone = phone;
		this.placeUrl = placeUrl;
		this.reservationUrl = reservationUrl;
		this.verified = verified;
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public PlaceProvider getProvider() {
		return provider;
	}

	public String getExternalPlaceId() {
		return externalPlaceId;
	}

	public PlaceType getPlaceType() {
		return placeType;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getRoadAddress() {
		return roadAddress;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public String getPhone() {
		return phone;
	}

	public String getPlaceUrl() {
		return placeUrl;
	}

	public String getReservationUrl() {
		return reservationUrl;
	}

	public boolean isVerified() {
		return verified;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
