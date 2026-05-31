package com.singlemungle.backend.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PlaceEntityMappingTest {

	@Autowired
	private EntityManager entityManager;

	@Test
	void persistPlaceWithRelatedTables() {
		Place place = new Place(
			PlaceProvider.KAKAO,
			"kakao-1",
			PlaceType.HOSPITAL,
			"싱글멍글 동물병원",
			"서울시 강남구",
			"서울시 강남구 테헤란로",
			new BigDecimal("37.1234567"),
			new BigDecimal("127.1234567"),
			"02-1234-5678",
			"https://place.example.com",
			"https://reservation.example.com",
			false
		);

		entityManager.persist(place);
		entityManager.persist(new PlaceImage(place, "https://image.example.com/place.jpg", 1));
		entityManager.persist(new PlaceBusinessHour(
			place,
			DayOfWeek.MONDAY,
			LocalTime.of(9, 0),
			LocalTime.of(18, 0),
			LocalTime.of(12, 0),
			LocalTime.of(13, 0),
			false
		));
		entityManager.persist(new PlaceService(place, "기본 진료", "반려동물 기본 진료 서비스"));
		entityManager.flush();
		entityManager.clear();

		Place savedPlace = entityManager.find(Place.class, place.getId());

		assertThat(savedPlace.getProvider()).isEqualTo(PlaceProvider.KAKAO);
		assertThat(savedPlace.getPlaceType()).isEqualTo(PlaceType.HOSPITAL);
		assertThat(savedPlace.getCreatedAt()).isNotNull();
		assertThat(savedPlace.getUpdatedAt()).isNotNull();
	}
}
