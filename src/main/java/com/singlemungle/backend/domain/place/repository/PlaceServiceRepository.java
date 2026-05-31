package com.singlemungle.backend.domain.place.repository;

import java.util.List;

import com.singlemungle.backend.domain.place.entity.PlaceService;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceServiceRepository extends JpaRepository<PlaceService, Long> {

	List<PlaceService> findByPlaceId(Long placeId);
}
