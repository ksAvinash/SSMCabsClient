package com.labs.ssmcabs.client.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class CoordinateAdapter {
    private String driver_name, driver_number, last_updated, vehicle_number, vehicle_type;
    private Double latitude, longitude;

    public CoordinateAdapter(){}

    public CoordinateAdapter(String driver_name, String driver_number, String vehicle_number, String vehicle_type){
        this.driver_name = driver_name;
        this.driver_number = driver_number;
        this.vehicle_type = vehicle_type;
        this.vehicle_number = vehicle_number;
    }


    public CoordinateAdapter(String driver_name, String driver_number, Double latitude, Double longitude, String vehicle_number, String vehicle_type) {
        this.driver_name = driver_name;
        this.driver_number = driver_number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vehicle_number = vehicle_number;
        this.vehicle_type = vehicle_type;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.last_updated = sdf.format(new Date());
    }


    public String getDriver_number() {
        return driver_number;
    }

    public String getLast_updated() {
        return last_updated;
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

    public String getDriver_name() {
        return driver_name;
    }

    public Double getLongitude() {
        return longitude;
    }
}

