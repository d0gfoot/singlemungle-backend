package com.singlemungle.backend.domain.place.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.singlemungle.backend.common.config.GooglePlacesProperties;
import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.place.client.GooglePlaceDetail;
import com.singlemungle.backend.domain.place.client.GooglePlaceSearchResult;
import com.singlemungle.backend.domain.place.client.GooglePlaceSummary;
import com.singlemungle.backend.domain.place.client.GooglePlacesClient;
import com.singlemungle.backend.domain.place.dto.GooglePlaceReviewAuthorResponse;
import com.singlemungle.backend.domain.place.dto.GooglePlaceReviewResponse;
import com.singlemungle.backend.domain.place.dto.NearbyPlaceItemResponse;
import com.singlemungle.backend.domain.place.dto.NearbyPlacesResponse;
import com.singlemungle.backend.domain.place.dto.PlaceDetailResponse;
import com.singlemungle.backend.domain.place.dto.PlacePhotoResponse;
import com.singlemungle.backend.domain.place.dto.PlacePhotoUriResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceQueryService {

	private static final double EARTH_RADIUS_METERS = 6_371_000;
	private static final Pattern PHOTO_NAME_PATTERN = Pattern.compile(
		"^places/[A-Za-z0-9_-]+/photos/[A-Za-z0-9_-]+$"
	);

	private final GooglePlacesClient googlePlacesClient;
	private final BeautyShopPlaceService beautyShopPlaceService;
	private final GooglePlacesProperties properties;

	public NearbyPlacesResponse findNearbyBeautyShops(
		BigDecimal latitude,
		BigDecimal longitude,
		Integer radiusMeters,
		Integer limit,
		String pageToken
	) {
		BigDecimal searchLatitude = latitude == null ? properties.getCenterLatitude() : latitude;
		BigDecimal searchLongitude = longitude == null ? properties.getCenterLongitude() : longitude;
		int searchRadius = radiusMeters == null ? properties.getDefaultRadiusMeters() : radiusMeters;
		int resultLimit = limit == null ? properties.getMaxResults() : limit;
		validateSearchOptions(searchRadius, resultLimit);

		GooglePlaceSearchResult result = googlePlacesClient.searchBeautyShops(
			searchLatitude, searchLongitude, searchRadius, resultLimit, pageToken
		);
		List<NearbyPlaceItemResponse> places = result.places().stream()
			.filter(place -> beautyShopPlaceService.isBeautyShopCandidate(place.name()))
			.filter(place -> place.latitude() != null && place.longitude() != null)
			.map(place -> toNearbyResponse(place, searchLatitude, searchLongitude))
			.filter(place -> place.distanceMeters() <= searchRadius)
			.sorted(Comparator.comparingLong(NearbyPlaceItemResponse::distanceMeters))
			.limit(resultLimit)
			.toList();
		return new NearbyPlacesResponse(places, result.nextPageToken());
	}

	public PlaceDetailResponse findPlaceDetail(String googlePlaceId) {
		GooglePlaceDetail detail = beautyShopPlaceService.getVerifiedBeautyShop(googlePlaceId);
		return new PlaceDetailResponse(
			detail.googlePlaceId(), detail.name(), detail.formattedAddress(), detail.latitude(), detail.longitude(),
			detail.phone(), detail.websiteUri(), detail.googleMapsUri(), detail.openNow(), detail.rating(),
			detail.userRatingCount(), detail.weekdayDescriptions(),
			detail.photos().stream()
				.map(photo -> new PlacePhotoResponse(photo.name(), photo.authorAttributions()))
				.toList(),
			detail.reviews().stream()
				.map(review -> new GooglePlaceReviewResponse(
					review.name(), review.rating(), review.text(), review.originalText(),
					review.relativePublishTimeDescription(), review.publishTime(),
					new GooglePlaceReviewAuthorResponse(
						review.author().displayName(), review.author().uri(), review.author().photoUri()
					),
					review.googleMapsUri(), review.flagContentUri()
				))
				.toList()
		);
	}

	public PlacePhotoUriResponse getPhotoUri(String photoName, int maxWidthPx) {
		if (photoName == null || !PHOTO_NAME_PATTERN.matcher(photoName).matches()
			|| maxWidthPx < 1 || maxWidthPx > 1600) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		return new PlacePhotoUriResponse(googlePlacesClient.getPhotoUri(photoName, maxWidthPx));
	}

	private void validateSearchOptions(int radiusMeters, int limit) {
		if (radiusMeters < 1 || radiusMeters > properties.getMaxRadiusMeters()
			|| limit < 1 || limit > properties.getMaxResults()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	private NearbyPlaceItemResponse toNearbyResponse(
		GooglePlaceSummary place,
		BigDecimal latitude,
		BigDecimal longitude
	) {
		long distance = calculateDistanceMeters(
			latitude.doubleValue(), longitude.doubleValue(),
			place.latitude().doubleValue(), place.longitude().doubleValue()
		);
		return new NearbyPlaceItemResponse(
			place.googlePlaceId(), place.name(), place.formattedAddress(), place.latitude(), place.longitude(),
			distance, place.googleMapsUri()
		);
	}

	private long calculateDistanceMeters(double sourceLat, double sourceLng, double targetLat, double targetLng) {
		double latitudeDistance = Math.toRadians(targetLat - sourceLat);
		double longitudeDistance = Math.toRadians(targetLng - sourceLng);
		double haversine = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
			+ Math.cos(Math.toRadians(sourceLat)) * Math.cos(Math.toRadians(targetLat))
			* Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
		return Math.round(EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine)));
	}
}
