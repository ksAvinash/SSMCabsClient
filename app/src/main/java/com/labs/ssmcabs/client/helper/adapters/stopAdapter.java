package com.labs.ssmcabs.client.helper.adapters;

public class stopAdapter {

    private String stop_name, locality, driver_number;
    private Double latitude, longitude;
    public stopAdapter(){}

    public stopAdapter(String driver_number, Double latitude, String locality, Double longitude) {
        this.locality = locality;
        this.driver_number = driver_number;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDriver_number() {
        return driver_number;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public String getStop_name() {
        return stop_name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getLocality() {
        return locality;
    }
}
