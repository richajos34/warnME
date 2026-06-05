package com.berkeley.irms.warnme.dto.warnme;

public class GeocodingResult {

    private final Double lat;
    private final Double lng;
    private final String formattedAddress;
    private final String status;
    private final String source;

    public GeocodingResult(Double lat, Double lng, String formattedAddress, String status, String source) {
        this.lat = lat;
        this.lng = lng;
        this.formattedAddress = formattedAddress;
        this.status = status;
        this.source = source;
    }

    public static GeocodingResult success(double lat, double lng, String formattedAddress, String source) {
        return new GeocodingResult(lat, lng, formattedAddress, "success", source);
    }

    public static GeocodingResult failed(String source) {
        return new GeocodingResult(null, null, null, "failed", source);
    }

    public boolean hasCoordinates() {
        return lat != null && lng != null;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getStatus() {
        return status;
    }

    public String getSource() {
        return source;
    }
}
