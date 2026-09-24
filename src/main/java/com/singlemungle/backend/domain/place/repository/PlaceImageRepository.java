package com.singlemungle.backend.domain.place.repository;

import java.util.List;

import com.singlemungle.backend.domain.place.entity.PlaceImage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

	List<PlaceImage> findByPlaceIdOrderBySortOrderAsc(Long placeId);
}
