package com.berkeley.irms.warnme.controllers;

import com.berkeley.irms.warnme.services.FlickrPhotoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final FlickrPhotoService flickrPhotoService;

    public PhotoController(FlickrPhotoService flickrPhotoService) {
        this.flickrPhotoService = flickrPhotoService;
    }

    @GetMapping("/nearby")
    public Map<String, Object> getNearbyPhotos(
            @RequestParam double lat,
            @RequestParam double lon) {
        List<String> photoUrls = flickrPhotoService.searchNearbyPhotoUrls(lat, lon);
        return Map.of(
                "count", photoUrls.size(),
                "photos", photoUrls);
    }
}
