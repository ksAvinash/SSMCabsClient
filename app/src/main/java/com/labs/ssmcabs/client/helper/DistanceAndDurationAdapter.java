package com.labs.ssmcabs.client.helper;

public class DistanceAndDurationAdapter {
    private String distance, duration;

    public DistanceAndDurationAdapter(String distance, String duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public DistanceAndDurationAdapter() {
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }
}
