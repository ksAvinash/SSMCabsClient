package com.labs.ssmcabs.client.helper.adapters;

public class BoardingConfigurationAdapter {
    private boolean dynamic_qr_code, location_trace, otp_to_client, otp_to_driver;

    public BoardingConfigurationAdapter() {
    }

    public BoardingConfigurationAdapter(boolean dynamic_qr_code, boolean location_trace, boolean otp_to_client, boolean otp_to_driver) {
        this.dynamic_qr_code = dynamic_qr_code;
        this.location_trace = location_trace;
        this.otp_to_client = otp_to_client;
        this.otp_to_driver = otp_to_driver;
    }

    public boolean isDynamic_qr_code() {
        return dynamic_qr_code;
    }

    public boolean isLocation_trace() {
        return location_trace;
    }

    public boolean isOtp_to_client() {
        return otp_to_client;
    }

    public boolean isOtp_to_driver() {
        return otp_to_driver;
    }
}
