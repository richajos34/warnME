package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.warnme.GeocodingResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

@Service
public class GeocodingService {

    private static final String GOOGLE_GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String MAPBOX_GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/{query}.json";
    private static final Map<String, GeocodingResult> CAMPUS_FALLBACKS = Map.of(
            "gilman hall", GeocodingResult.success(
                    37.8726,
                    -122.2565,
                    "South Drive near Gilman Hall, UC Berkeley, Berkeley, CA",
                    "campus_fallback"));

    private final String googleMapsApiKey;
    private final String mapboxAccessToken;
    private final RestTemplate restTemplate;

    @Autowired
    public GeocodingService(
            @Value("${google.maps.api-key:}") String googleMapsApiKey,
            @Value("${mapbox.access-token:}") String mapboxAccessToken) {
        this(googleMapsApiKey, mapboxAccessToken, new RestTemplate());
    }

    GeocodingService(String googleMapsApiKey, String mapboxAccessToken, RestTemplate restTemplate) {
        this.googleMapsApiKey = googleMapsApiKey;
        this.mapboxAccessToken = mapboxAccessToken;
        this.restTemplate = restTemplate;
    }

    public GeocodingResult geocodeLocation(String locationText) {
        if (locationText == null || locationText.isBlank()) {
            return GeocodingResult.failed("none");
        }

        GeocodingResult fallbackResult = fallbackCampusGeocode(locationText);

        if (googleMapsApiKey != null && !googleMapsApiKey.isBlank()) {
            GeocodingResult googleResult = geocodeWithGoogle(locationText);
            if (googleResult.hasCoordinates()) {
                return googleResult;
            }
        }

        if (mapboxAccessToken != null && !mapboxAccessToken.isBlank()) {
            GeocodingResult mapboxResult = geocodeWithMapbox(locationText);
            if (mapboxResult.hasCoordinates()) {
                return mapboxResult;
            }
        }

        return fallbackResult.hasCoordinates() ? fallbackResult : GeocodingResult.failed("none");
    }

    private GeocodingResult geocodeWithGoogle(String locationText) {
        URI uri = UriComponentsBuilder.fromUriString(GOOGLE_GEOCODING_URL)
                .queryParam("address", locationText)
                .queryParam("key", googleMapsApiKey)
                .build()
                .encode()
                .toUri();

        try {
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            JsonNode firstResult = response == null ? null : response.path("results").path(0);
            if (firstResult == null || firstResult.isMissingNode()) {
                return GeocodingResult.failed("google");
            }

            JsonNode location = firstResult.path("geometry").path("location");
            if (!location.has("lat") || !location.has("lng")) {
                return GeocodingResult.failed("google");
            }

            return GeocodingResult.success(
                    location.path("lat").asDouble(),
                    location.path("lng").asDouble(),
                    firstResult.path("formatted_address").asText(locationText),
                    "google");
        } catch (RestClientException exception) {
            return GeocodingResult.failed("google");
        }
    }

    private GeocodingResult geocodeWithMapbox(String locationText) {
        URI uri = UriComponentsBuilder.fromUriString(MAPBOX_GEOCODING_URL)
                .queryParam("access_token", mapboxAccessToken)
                .queryParam("limit", "1")
                .build(locationText);

        try {
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            JsonNode firstFeature = response == null ? null : response.path("features").path(0);
            JsonNode center = firstFeature == null ? null : firstFeature.path("center");
            if (center == null || !center.isArray() || center.size() < 2) {
                return GeocodingResult.failed("mapbox");
            }

            return GeocodingResult.success(
                    center.path(1).asDouble(),
                    center.path(0).asDouble(),
                    firstFeature.path("place_name").asText(locationText),
                    "mapbox");
        } catch (RestClientException exception) {
            return GeocodingResult.failed("mapbox");
        }
    }

    private GeocodingResult fallbackCampusGeocode(String locationText) {
        String normalized = locationText.toLowerCase(Locale.ROOT);
        return CAMPUS_FALLBACKS.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(() -> GeocodingResult.failed("campus_fallback"));
    }
}
