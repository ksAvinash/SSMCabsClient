package com.labs.ssmcabs.client.helper;

public class stopAdapter {

    private String stop_name, driver_name, driver_number, vehicle_number, vehicle_type;
    private Double latitude, longitude;
    public stopAdapter(){}

    public stopAdapter(String stop_name, String driver_name, String driver_number, String vehicle_number, String vehicle_type, Double latitude, Double longitude) {
        this.stop_name = stop_name;
        this.driver_name = driver_name;
        this.driver_number = driver_number;
        this.vehicle_number = vehicle_number;
        this.vehicle_type = vehicle_type;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public stopAdapter(String driver_name, String driver_number, String vehicle_number, String vehicle_type, Double latitude, Double longitude) {
        this.driver_name = driver_name;
        this.driver_number = driver_number;
        this.vehicle_number = vehicle_number;
        this.vehicle_type = vehicle_type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public String getStop_name() {
        return stop_name;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public String getDriver_number() {
        return driver_number;
    }

    public String getVehicle_number() {
        return vehicle_number;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
