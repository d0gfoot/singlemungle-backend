package com.singlemungle.backend.domain.place.repository;

import java.util.List;
import java.util.Optional;

import com.singlemungle.backend.domain.place.entity.Place;
import com.singlemungle.backend.domain.place.entity.PlaceType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

	List<Place> findByPlaceTypeIn(List<PlaceType> placeTypes);

	Optional<Place> findByIdAndPlaceType(Long id, PlaceType placeType);

	@Query("""
		select p
		from Place p
		where p.placeType in :placeTypes
			and p.latitude between :minLatitude and :maxLatitude
			and p.longitude between :minLongitude and :maxLongitude
		""")
	List<Place> findByPlaceTypeInAndLocationBetween(
		@Param("placeTypes") List<PlaceType> placeTypes,
		@Param("minLatitude") java.math.BigDecimal minLatitude,
		@Param("maxLatitude") java.math.BigDecimal maxLatitude,
		@Param("minLongitude") java.math.BigDecimal minLongitude,
		@Param("maxLongitude") java.math.BigDecimal maxLongitude
	);

	Page<Place> findByNameContainingIgnoreCase(String name, Pageable pageable);

	Page<Place> findByPlaceTypeIn(List<PlaceType> placeTypes, Pageable pageable);

	Page<Place> findByNameContainingIgnoreCaseAndPlaceTypeIn(
		String name,
		List<PlaceType> placeTypes,
		Pageable pageable
	);
}
