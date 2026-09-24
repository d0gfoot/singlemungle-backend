package com.singlemungle.backend.domain.place.repository;

import java.util.Optional;

import com.singlemungle.backend.domain.place.entity.FavoritePlace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

	Page<FavoritePlace> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	Optional<FavoritePlace> findByIdAndUserId(Long id, Long userId);

	boolean existsByUserIdAndGooglePlaceId(Long userId, String googlePlaceId);
}
