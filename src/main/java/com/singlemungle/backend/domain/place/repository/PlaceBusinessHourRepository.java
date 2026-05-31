package com.singlemungle.backend.domain.place.repository;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.List;

import com.singlemungle.backend.domain.place.entity.PlaceBusinessHour;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceBusinessHourRepository extends JpaRepository<PlaceBusinessHour, Long> {

	List<PlaceBusinessHour> findByPlaceIdInAndDayOfWeek(Collection<Long> placeIds, DayOfWeek dayOfWeek);

	List<PlaceBusinessHour> findByPlaceId(Long placeId);
}
