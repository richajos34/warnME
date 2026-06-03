package com.berkeley.irms.warnme.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

@Service
public class FlickrPhotoService {

    private static final String FLICKR_SEARCH_URL = "https://www.flickr.com/services/rest/";

    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public FlickrPhotoService(@Value("${flickr.api.key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> searchNearbyPhotoUrls(double latitude, double longitude) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(PRECONDITION_FAILED, "FLICKR_API_KEY is not configured.");
        }

        String url = UriComponentsBuilder.fromUriString(FLICKR_SEARCH_URL)
                .queryParam("method", "flickr.photos.search")
                .queryParam("api_key", apiKey)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("format", "json")
                .queryParam("nojsoncallback", "1")
                .queryParam("per_page", "10")
                .queryParam("sort", "date-posted-desc")
                .build()
                .encode()
                .toUriString();

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return extractPhotoUrls(response);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Flickr photo search failed.", exception);
        }
    }

    private List<String> extractPhotoUrls(JsonNode response) {
        List<String> photoUrls = new ArrayList<>();
        JsonNode photos = response == null ? null : response.path("photos").path("photo");

        if (photos == null || !photos.isArray()) {
            return photoUrls;
        }

        for (JsonNode photo : photos) {
            photoUrls.add(String.format(
                    "https://farm%s.staticflickr.com/%s/%s_%s.jpg",
                    photo.path("farm").asText(),
                    photo.path("server").asText(),
                    photo.path("id").asText(),
                    photo.path("secret").asText()));
        }

        return photoUrls;
    }
}
