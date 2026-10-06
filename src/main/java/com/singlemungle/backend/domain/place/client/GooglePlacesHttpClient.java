package com.singlemungle.backend.domain.place.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.singlemungle.backend.common.config.GooglePlacesProperties;
import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.google-places.enabled", havingValue = "true")
public class GooglePlacesHttpClient implements GooglePlacesClient {

	private static final String BASE_URL = "https://places.googleapis.com/v1";
	private static final String SEARCH_FIELD_MASK = String.join(",",
		"places.id",
		"places.displayName",
		"places.formattedAddress",
		"places.location",
		"places.rating",
		"places.userRatingCount",
		"places.googleMapsUri",
		"nextPageToken"
	);
	private static final String DETAIL_FIELD_MASK = String.join(",",
		"id",
		"displayName",
		"formattedAddress",
		"location",
		"nationalPhoneNumber",
		"websiteUri",
		"googleMapsUri",
		"currentOpeningHours.openNow",
		"regularOpeningHours.weekdayDescriptions",
		"rating",
		"userRatingCount",
		"types",
		"photos.name",
		"photos.authorAttributions",
		"reviews.name",
		"reviews.rating",
		"reviews.text",
		"reviews.originalText",
		"reviews.relativePublishTimeDescription",
		"reviews.publishTime",
		"reviews.authorAttribution",
		"reviews.googleMapsUri",
		"reviews.flagContentUri"
	);

	private final GooglePlacesProperties properties;
	private final RestClient restClient;

	public GooglePlacesHttpClient(GooglePlacesProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());
		requestFactory.setConnectTimeout(timeout);
		requestFactory.setReadTimeout(timeout);
		this.restClient = RestClient.builder()
			.baseUrl(BASE_URL)
			.requestFactory(requestFactory)
			.defaultHeader("X-Goog-Api-Key", properties.getApiKey())
			.build();
	}

	@Override
	public GooglePlaceSearchResult searchBeautyShops(
		BigDecimal latitude,
		BigDecimal longitude,
		int radiusMeters,
		int pageSize,
		String pageToken
	) {
		return searchBeautyShops(properties.getBeautySearchQuery(), latitude, longitude, radiusMeters, pageSize, pageToken);
	}

	@Override
	public GooglePlaceSearchResult searchBeautyShopsByName(
		String query,
		int pageSize,
		String pageToken
	) {
		return searchBeautyShops(query, null, null, null, pageSize, pageToken);
	}

	@Override
	public GooglePlaceSearchResult searchNearbyBeautyShopsByName(
		String query,
		BigDecimal latitude,
		BigDecimal longitude,
		int radiusMeters,
		int pageSize,
		String pageToken
	) {
		return searchBeautyShops(query, latitude, longitude, radiusMeters, pageSize, pageToken);
	}

	private GooglePlaceSearchResult searchBeautyShops(
		String textQuery,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer radiusMeters,
		int pageSize,
		String pageToken
	) {
		try {
			Map<String, Object> response = restClient.post()
				.uri("/places:searchText")
				.header(HttpHeaders.CONTENT_TYPE, "application/json")
				.header("X-Goog-FieldMask", SEARCH_FIELD_MASK)
				.body(searchRequest(textQuery, latitude, longitude, radiusMeters, pageSize, pageToken))
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

			List<GooglePlaceSummary> places = asList(value(response, "places")).stream()
				.map(this::asMap)
				.map(this::toSummary)
				.toList();
			return new GooglePlaceSearchResult(places, asString(value(response, "nextPageToken")));
		} catch (RestClientResponseException exception) {
			throw translate(exception, "search");
		} catch (ResourceAccessException exception) {
			log.warn("Google Places search timed out", exception);
			throw new BusinessException(ErrorCode.PLACE_PROVIDER_TIMEOUT);
		}
	}

	@Override
	public GooglePlaceDetail getPlaceDetail(String googlePlaceId) {
		try {
			Map<String, Object> response = restClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/places/{placeId}")
					.queryParam("languageCode", properties.getLanguageCode())
					.queryParam("regionCode", properties.getRegionCode())
					.build(googlePlaceId))
				.header("X-Goog-FieldMask", DETAIL_FIELD_MASK)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
			return toDetail(response);
		} catch (RestClientResponseException exception) {
			throw translate(exception, "detail");
		} catch (ResourceAccessException exception) {
			log.warn("Google Places detail timed out", exception);
			throw new BusinessException(ErrorCode.PLACE_PROVIDER_TIMEOUT);
		}
	}

	@Override
	public String getPhotoUri(String photoName, int maxWidthPx) {
		try {
			Map<String, Object> response = restClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/" + photoName + "/media")
					.queryParam("maxWidthPx", maxWidthPx)
					.queryParam("skipHttpRedirect", true)
					.build())
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
			String photoUri = asString(value(response, "photoUri"));
			if (photoUri == null || photoUri.isBlank()) {
				throw new BusinessException(ErrorCode.PLACE_PROVIDER_UNAVAILABLE);
			}
			return photoUri;
		} catch (RestClientResponseException exception) {
			throw translate(exception, "photo");
		} catch (ResourceAccessException exception) {
			log.warn("Google Places photo request timed out", exception);
			throw new BusinessException(ErrorCode.PLACE_PROVIDER_TIMEOUT);
		}
	}

	private Map<String, Object> searchRequest(
		String textQuery,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer radiusMeters,
		int pageSize,
		String pageToken
	) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("textQuery", textQuery);
		body.put("includedType", "pet_care");
		body.put("languageCode", properties.getLanguageCode());
		body.put("regionCode", properties.getRegionCode());
		body.put("pageSize", pageSize);
		body.put("rankPreference", latitude == null ? "RELEVANCE" : "DISTANCE");
		body.put("includePureServiceAreaBusinesses", false);
		if (latitude != null && longitude != null && radiusMeters != null) {
			body.put("locationBias", Map.of("circle", Map.of(
				"center", Map.of("latitude", latitude, "longitude", longitude),
				"radius", radiusMeters
			)));
		}
		if (pageToken != null && !pageToken.isBlank()) {
			body.put("pageToken", pageToken);
		}
		return body;
	}

	private GooglePlaceSummary toSummary(Map<String, Object> place) {
		Map<String, Object> location = asMap(place.get("location"));
		Integer userRatingCount = asInteger(place.get("userRatingCount"));
		return new GooglePlaceSummary(
			asString(place.get("id")),
			displayName(place),
			asString(place.get("formattedAddress")),
			asBigDecimal(location.get("latitude")),
			asBigDecimal(location.get("longitude")),
			asBigDecimal(place.get("rating")),
			userRatingCount == null ? 0 : userRatingCount,
			asString(place.get("googleMapsUri"))
		);
	}

	GooglePlaceDetail toDetail(Map<String, Object> place) {
		Map<String, Object> location = asMap(place.get("location"));
		Map<String, Object> currentHours = asMap(place.get("currentOpeningHours"));
		Map<String, Object> regularHours = asMap(place.get("regularOpeningHours"));
		return new GooglePlaceDetail(
			asString(place.get("id")),
			displayName(place),
			asString(place.get("formattedAddress")),
			asBigDecimal(location.get("latitude")),
			asBigDecimal(location.get("longitude")),
			asString(place.get("nationalPhoneNumber")),
			asString(place.get("websiteUri")),
			asString(place.get("googleMapsUri")),
			asBoolean(currentHours.get("openNow")),
			asBigDecimal(place.get("rating")),
			asInteger(place.get("userRatingCount")),
			asList(place.get("types")).stream().map(this::asString).toList(),
			asList(regularHours.get("weekdayDescriptions")).stream().map(this::asString).toList(),
			photos(place.get("photos")),
			reviews(place.get("reviews"))
		);
	}

	private List<GooglePlaceReview> reviews(Object value) {
		List<GooglePlaceReview> reviews = new ArrayList<>();
		for (Object reviewValue : asList(value)) {
			Map<String, Object> review = asMap(reviewValue);
			Map<String, Object> author = asMap(review.get("authorAttribution"));
			reviews.add(new GooglePlaceReview(
				asString(review.get("name")),
				asBigDecimal(review.get("rating")),
				localizedText(review.get("text")),
				localizedText(review.get("originalText")),
				asString(review.get("relativePublishTimeDescription")),
				asString(review.get("publishTime")),
				new GooglePlaceReviewAuthor(
					asString(author.get("displayName")),
					asString(author.get("uri")),
					asString(author.get("photoUri"))
				),
				asString(review.get("googleMapsUri")),
				asString(review.get("flagContentUri"))
			));
		}
		return reviews;
	}

	private List<GooglePlacePhoto> photos(Object value) {
		List<GooglePlacePhoto> photos = new ArrayList<>();
		for (Object photoValue : asList(value)) {
			Map<String, Object> photo = asMap(photoValue);
			List<String> attributions = asList(photo.get("authorAttributions")).stream()
				.map(this::asMap)
				.map(attribution -> asString(attribution.get("displayName")))
				.filter(name -> name != null && !name.isBlank())
				.toList();
			photos.add(new GooglePlacePhoto(asString(photo.get("name")), attributions));
		}
		return photos;
	}

	private RuntimeException translate(RestClientResponseException exception, String operation) {
		log.warn("Google Places {} failed status={}", operation, exception.getStatusCode().value());
		if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
			return new BusinessException(ErrorCode.PLACE_NOT_FOUND);
		}
		if (exception.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
			return new BusinessException(ErrorCode.PLACE_PROVIDER_RATE_LIMITED);
		}
		return new BusinessException(ErrorCode.PLACE_PROVIDER_UNAVAILABLE);
	}

	private String displayName(Map<String, Object> place) {
		return asString(asMap(place.get("displayName")).get("text"));
	}

	private String localizedText(Object value) {
		return asString(asMap(value).get("text"));
	}

	private Object value(Map<String, Object> map, String key) {
		return map == null ? null : map.get(key);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		return value instanceof Map<?, ?> ? (Map<String, Object>)value : Map.of();
	}

	@SuppressWarnings("unchecked")
	private List<Object> asList(Object value) {
		return value instanceof List<?> ? (List<Object>)value : List.of();
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private BigDecimal asBigDecimal(Object value) {
		return value == null ? null : new BigDecimal(String.valueOf(value));
	}

	private Integer asInteger(Object value) {
		return value instanceof Number number ? number.intValue() : null;
	}

	private Boolean asBoolean(Object value) {
		return value instanceof Boolean booleanValue ? booleanValue : null;
	}
}
