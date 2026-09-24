package com.singlemungle.backend.common.config;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.google-places")
public class GooglePlacesProperties {

	private boolean enabled;
	private String apiKey = "";
	private String languageCode = "ko";
	private String regionCode = "KR";
	private int timeoutSeconds = 5;
	private BigDecimal centerLatitude = new BigDecimal("37.5000530744517");
	private BigDecimal centerLongitude = new BigDecimal("126.86799268384159");
	private int defaultRadiusMeters = 10000;
	private int maxRadiusMeters = 10000;
	private int maxResults = 20;
	private String beautySearchQuery = "dog grooming";
}
