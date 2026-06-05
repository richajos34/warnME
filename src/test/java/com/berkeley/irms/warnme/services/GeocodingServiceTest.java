package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.warnme.GeocodingResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class GeocodingServiceTest {

    @Test
    void usesCampusFallbackForGilmanHall() {
        GeocodingService geocodingService = new GeocodingService("", "", new RestTemplate());

        GeocodingResult result = geocodingService.geocodeLocation("South Drive near Gilman Hall, UC Berkeley, Berkeley, CA");

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getSource()).isEqualTo("campus_fallback");
        assertThat(result.getLat()).isNotNull();
        assertThat(result.getLng()).isNotNull();
    }
}
