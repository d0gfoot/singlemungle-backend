package com.singlemungle.backend.domain.place.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.place.dto.BusinessHourResponse;
import com.singlemungle.backend.domain.place.dto.NearbyPlaceItemResponse;
import com.singlemungle.backend.domain.place.dto.NearbyPlacesResponse;
import com.singlemungle.backend.domain.place.dto.PlaceDetailResponse;
import com.singlemungle.backend.domain.place.dto.PlaceListItemResponse;
import com.singlemungle.backend.domain.place.entity.Place;
import com.singlemungle.backend.domain.place.entity.PlaceBusinessHour;
import com.singlemungle.backend.domain.place.entity.PlaceImage;
import com.singlemungle.backend.domain.place.entity.PlaceService;
import com.singlemungle.backend.domain.place.entity.PlaceType;
import com.singlemungle.backend.domain.place.repository.PlaceBusinessHourRepository;
import com.singlemungle.backend.domain.place.repository.PlaceImageRepository;
import com.singlemungle.backend.domain.place.repository.PlaceRepository;
import com.singlemungle.backend.domain.place.repository.PlaceServiceRepository;
import com.singlemungle.backend.domain.place.service.MockPlaceReviewService.ReviewStat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlaceQueryService {

	private static final double EARTH_RADIUS_METERS = 6_371_000;
	private static final BigDecimal METERS_PER_LATITUDE_DEGREE = new BigDecimal("111320");
	private static final int DEFAULT_RADIUS_METERS = 3_000;
	private static final int DEFAULT_LIMIT = 20;
	private static final List<PlaceType> DEFAULT_NEARBY_PLACE_TYPES = List.of(PlaceType.HOSPITAL, PlaceType.BEAUTY);

	private final PlaceRepository placeRepository;
	private final PlaceBusinessHourRepository placeBusinessHourRepository;
	private final PlaceServiceRepository placeServiceRepository;
	private final PlaceImageRepository placeImageRepository;
	private final MockPlaceReviewService mockPlaceReviewService;
	private final Clock clock;

	public PlaceQueryService(
		PlaceRepository placeRepository,
		PlaceBusinessHourRepository placeBusinessHourRepository,
		PlaceServiceRepository placeServiceRepository,
		PlaceImageRepository placeImageRepository,
		MockPlaceReviewService mockPlaceReviewService,
		Clock clock
	) {
		this.placeRepository = placeRepository;
		this.placeBusinessHourRepository = placeBusinessHourRepository;
		this.placeServiceRepository = placeServiceRepository;
		this.placeImageRepository = placeImageRepository;
		this.mockPlaceReviewService = mockPlaceReviewService;
		this.clock = clock;
	}

	public PageResponse<PlaceListItemResponse> searchPlaces(
		String keyword,
		List<PlaceType> placeTypes,
		Pageable pageable
	) {
		Page<Place> places = searchPlaceEntities(keyword, placeTypes, pageable);
		Map<Long, PlaceBusinessHour> businessHours = findTodayBusinessHours(places.getContent());

		return PageResponse.from(places.map(place -> toPlaceListItem(place, businessHours.get(place.getId()))));
	}

	private Page<Place> searchPlaceEntities(String keyword, List<PlaceType> placeTypes, Pageable pageable) {
		boolean hasKeyword = keyword != null && !keyword.isBlank();
		boolean hasPlaceTypes = placeTypes != null && !placeTypes.isEmpty();

		if (hasKeyword && hasPlaceTypes) {
			return placeRepository.findByNameContainingIgnoreCaseAndPlaceTypeIn(keyword.trim(), placeTypes, pageable);
		}
		if (hasKeyword) {
			return placeRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
		}
		if (hasPlaceTypes) {
			return placeRepository.findByPlaceTypeIn(placeTypes, pageable);
		}
		return placeRepository.findAll(pageable);
	}

	public PlaceDetailResponse findPlaceDetail(Long placeId) {
		Place place = placeRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
		List<PlaceBusinessHour> weeklyBusinessHours = placeBusinessHourRepository.findByPlaceId(placeId).stream()
			.sorted(Comparator.comparingInt(businessHour -> businessHour.getDayOfWeek().getValue()))
			.toList();
		Map<Long, PlaceBusinessHour> todayBusinessHour = findTodayBusinessHours(List.of(place));

		List<String> services = placeServiceRepository.findByPlaceId(placeId).stream()
			.map(PlaceService::getServiceName)
			.toList();
		List<String> images = placeImageRepository.findByPlaceIdOrderBySortOrderAsc(placeId).stream()
			.map(PlaceImage::getImageUrl)
			.toList();

		PlaceBusinessHour today = todayBusinessHour.get(placeId);

		return new PlaceDetailResponse(
			place.getId(),
			place.getName(),
			place.getPhone(),
			formatTime(today == null ? null : today.getOpenTime()),
			formatTime(today == null ? null : today.getCloseTime()),
			services,
			place.getPlaceUrl(),
			place.getReservationUrl(),
			images,
			weeklyBusinessHours.stream()
				.map(this::toBusinessHourResponse)
				.toList()
		);
	}

	public NearbyPlacesResponse findNearbyPlaces(
		BigDecimal latitude,
		BigDecimal longitude,
		Integer radiusMeters,
		List<PlaceType> placeTypes,
		Integer limit
	) {
		int searchRadiusMeters = radiusMeters == null ? DEFAULT_RADIUS_METERS : radiusMeters;
		int resultLimit = limit == null ? DEFAULT_LIMIT : limit;
		List<PlaceType> searchPlaceTypes = placeTypes == null || placeTypes.isEmpty()
			? DEFAULT_NEARBY_PLACE_TYPES
			: placeTypes;
		LocationBounds bounds = calculateLocationBounds(latitude, longitude, searchRadiusMeters);
		List<Place> places = placeRepository.findByPlaceTypeInAndLocationBetween(
			searchPlaceTypes,
			bounds.minLatitude(),
			bounds.maxLatitude(),
			bounds.minLongitude(),
			bounds.maxLongitude()
		);
		Map<Long, PlaceBusinessHour> businessHours = findTodayBusinessHours(places);
		LocalTime now = LocalTime.now(clock);

		List<NearbyPlaceItemResponse> nearbyPlaces = places.stream()
			.map(place -> toNearbyPlaceItem(place, latitude, longitude, businessHours.get(place.getId()), now))
			.filter(response -> response.distanceMeters() <= searchRadiusMeters)
			.sorted(Comparator.comparingLong(NearbyPlaceItemResponse::distanceMeters))
			.limit(resultLimit)
			.toList();

		return new NearbyPlacesResponse(nearbyPlaces);
	}

	private LocationBounds calculateLocationBounds(
		BigDecimal latitude,
		BigDecimal longitude,
		int radiusMeters
	) {
		BigDecimal latitudeDelta = BigDecimal.valueOf(radiusMeters)
			.divide(METERS_PER_LATITUDE_DEGREE, 7, RoundingMode.CEILING);
		double latitudeRadians = Math.toRadians(latitude.doubleValue());
		double longitudeDegreeMeters = METERS_PER_LATITUDE_DEGREE.doubleValue() * Math.cos(latitudeRadians);
		BigDecimal longitudeDelta = BigDecimal.valueOf(radiusMeters / longitudeDegreeMeters)
			.setScale(7, RoundingMode.CEILING);

		return new LocationBounds(
			latitude.subtract(latitudeDelta),
			latitude.add(latitudeDelta),
			longitude.subtract(longitudeDelta),
			longitude.add(longitudeDelta)
		);
	}

	private Map<Long, PlaceBusinessHour> findTodayBusinessHours(List<Place> places) {
		List<Long> placeIds = places.stream()
			.map(Place::getId)
			.toList();

		if (placeIds.isEmpty()) {
			return Map.of();
		}

		DayOfWeek today = DayOfWeek.from(clock.instant().atZone(clock.getZone()));

		return placeBusinessHourRepository.findByPlaceIdInAndDayOfWeek(placeIds, today).stream()
			.collect(Collectors.toMap(
				businessHour -> businessHour.getPlace().getId(),
				Function.identity()
			));
	}

	private NearbyPlaceItemResponse toNearbyPlaceItem(
		Place place,
		BigDecimal latitude,
		BigDecimal longitude,
		PlaceBusinessHour businessHour,
		LocalTime now
	) {
		ReviewStat reviewStat = mockPlaceReviewService.getReviewStat(place.getExternalPlaceId());
		long distanceMeters = calculateDistanceMeters(
			latitude.doubleValue(),
			longitude.doubleValue(),
			place.getLatitude().doubleValue(),
			place.getLongitude().doubleValue()
		);

		return new NearbyPlaceItemResponse(
			place.getId(),
			place.getExternalPlaceId(),
			place.getPlaceType(),
			place.getName(),
			place.getLatitude(),
			place.getLongitude(),
			distanceMeters,
			isOpen(businessHour, now),
			reviewStat.ratingAverage(),
			reviewStat.reviewCount()
		);
	}

	private PlaceListItemResponse toPlaceListItem(Place place, PlaceBusinessHour businessHour) {
		ReviewStat reviewStat = mockPlaceReviewService.getReviewStat(place.getExternalPlaceId());
		LocalTime now = LocalTime.now(clock);

		return new PlaceListItemResponse(
			place.getId(),
			place.getPlaceType(),
			place.getName(),
			place.getAddress(),
			place.getRoadAddress(),
			place.getLatitude(),
			place.getLongitude(),
			reviewStat.ratingAverage(),
			reviewStat.reviewCount(),
			isOpen(businessHour, now),
			businessHour == null ? null : businessHour.getDayOfWeek().name(),
			businessHour == null ? null : formatTime(businessHour.getOpenTime()),
			businessHour == null ? null : formatTime(businessHour.getCloseTime())
		);
	}

	private BusinessHourResponse toBusinessHourResponse(PlaceBusinessHour businessHour) {
		return new BusinessHourResponse(
			businessHour.getDayOfWeek().name(),
			formatTime(businessHour.getOpenTime()),
			formatTime(businessHour.getCloseTime())
		);
	}

	private String formatTime(LocalTime time) {
		if (time == null) {
			return null;
		}
		return time.toString();
	}

	private boolean isOpen(PlaceBusinessHour businessHour, LocalTime now) {
		if (businessHour == null || businessHour.isClosed()) {
			return false;
		}
		if (businessHour.getOpenTime() == null || businessHour.getCloseTime() == null) {
			return false;
		}
		if (now.isBefore(businessHour.getOpenTime()) || !now.isBefore(businessHour.getCloseTime())) {
			return false;
		}
		if (businessHour.getBreakStartTime() == null || businessHour.getBreakEndTime() == null) {
			return true;
		}
		return now.isBefore(businessHour.getBreakStartTime()) || !now.isBefore(businessHour.getBreakEndTime());
	}

	private long calculateDistanceMeters(
		double sourceLatitude,
		double sourceLongitude,
		double targetLatitude,
		double targetLongitude
	) {
		double sourceLatitudeRadians = Math.toRadians(sourceLatitude);
		double targetLatitudeRadians = Math.toRadians(targetLatitude);
		double latitudeDelta = Math.toRadians(targetLatitude - sourceLatitude);
		double longitudeDelta = Math.toRadians(targetLongitude - sourceLongitude);

		double haversine = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
			+ Math.cos(sourceLatitudeRadians) * Math.cos(targetLatitudeRadians)
			* Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
		double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

		return Math.round(EARTH_RADIUS_METERS * centralAngle);
	}

	private record LocationBounds(
		BigDecimal minLatitude,
		BigDecimal maxLatitude,
		BigDecimal minLongitude,
		BigDecimal maxLongitude
	) {
	}
}
