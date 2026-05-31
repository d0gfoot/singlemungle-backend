package com.singlemungle.backend.domain.place.repository;

import java.util.List;

import com.singlemungle.backend.domain.place.entity.Place;
import com.singlemungle.backend.domain.place.entity.PlaceProvider;
import com.singlemungle.backend.domain.place.entity.PlaceType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

	List<Place> findByPlaceTypeIn(List<PlaceType> placeTypes);

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

	List<Place> findByProviderAndExternalPlaceIdIn(PlaceProvider provider, List<String> externalPlaceIds);
}
