package com.singlemungle.backend.domain.place.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoritePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

	@Column(name = "google_place_id", nullable = false, length = 255)
	private String googlePlaceId;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

	public FavoritePlace(Long userId, String googlePlaceId, String memo) {
        this.userId = userId;
		this.googlePlaceId = googlePlaceId;
        this.memo = memo;
    }

	public void updateMemo(String memo) {
		this.memo = memo;
	}
}
