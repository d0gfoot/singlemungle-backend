package com.singlemungle.backend.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.domain.place.client.GooglePlaceDetail;
import com.singlemungle.backend.domain.place.client.GooglePlacesClient;

import org.junit.jupiter.api.Test;

class BeautyShopPlaceServiceTest {

	private final GooglePlacesClient googlePlacesClient = mock(GooglePlacesClient.class);
	private final BeautyShopPlaceService service = new BeautyShopPlaceService(googlePlacesClient);

	@Test
	void acceptsPetCarePlaceWithoutKnownGroomingKeyword() {
		when(googlePlacesClient.getPlaceDetail("place-id"))
			.thenReturn(place("그루밍솔루션 개봉점", List.of("pet_care", "establishment")));

		GooglePlaceDetail result = service.getVerifiedBeautyShop(" place-id ");

		assertThat(result.name()).isEqualTo("그루밍솔루션 개봉점");
	}

	@Test
	void rejectsNonPetCarePlace() {
		when(googlePlacesClient.getPlaceDetail("place-id"))
			.thenReturn(place("사람 미용실", List.of("hair_salon")));

		assertThatThrownBy(() -> service.getVerifiedBeautyShop("place-id"))
			.isInstanceOf(BusinessException.class);
	}

	private GooglePlaceDetail place(String name, List<String> types) {
		return new GooglePlaceDetail(
			"place-id", name, "서울", BigDecimal.ONE, BigDecimal.ONE,
			null, null, null, null, null, null, types, List.of(), List.of()
		);
	}
}
