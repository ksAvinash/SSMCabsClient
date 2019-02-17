package com.labs.ssmcabs.client.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferenceHelper {

    public static String fetchDriverPhoneNo(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("driver_number", "");
    }

    public static String fetchDriverVehicleNumber(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("vehicle_number", "");
    }

    public static String fetchUserName(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("user_name", "");
    }


    public static String fetchDriverName(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("driver_name", "");
    }

    public static Double fetchMyStopLatitude(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return Double.longBitsToDouble(sharedPreferences.getLong("latitude", Double.doubleToLongBits(0)));
    }


    public static Double fetchMyStopLongitude(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return Double.longBitsToDouble(sharedPreferences.getLong("longitude", Double.doubleToLongBits(0)));
    }


    public static String fetchUserPhoneNumber(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("phone_number", "");
    }

    public static boolean fetchPhoneNumberVisibilityStatus(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_phone_number_visible", false);
    }

    public static String fetchStopName(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("stop_name", "dummy_stop");
    }

    public static boolean isStopSetupComplete(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_setup_complete", false);
    }







    public static boolean isLastBoardDateValid(Context context){
        Date date = new Date();
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a", Locale.getDefault());

        try {
            Date last_board_date = time_formatter.parse(sharedPreferences.getString("last_board_time", ""));
            long difference = date.getTime() - last_board_date.getTime();
            final int diffInDays = (int)(difference / (1000 * 60 * 60 * 24));
            Log.d("LAST_BOARD", "days : "+diffInDays);

            return diffInDays < 3;
        }catch (ParseException e){
            return false;
        }
    }






    public static void saveLastBoardTime(Context context, String last_board_time){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("last_board_time", last_board_time);
        editor.apply();
    }




    public static void saveDriverNumber(Context context, String stop_name, String driver_number){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("stop_name", stop_name);
        editor.putString("driver_number", driver_number);
        editor.apply();
    }

    public static void saveDriverDetails(Context context, String driver_name, String vehicle_number, String vehicle_type){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("driver_name", driver_name);
        editor.putString("vehicle_number", vehicle_number);
        editor.putString("vehicle_type", vehicle_type);
        editor.apply();
    }

    public static void saveUserProfileDetails(Context context, String user_name, String phone_number){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", user_name);
        editor.putString("phone_number", phone_number);
        editor.putBoolean("is_phone_number_visible", true);
        editor.putBoolean("is_setup_complete", true);
        editor.apply();
    }

    public static void saveStopDetails(Context context, stopAdapter selected_stop){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("stop_name", selected_stop.getStop_name());
        editor.putString("driver_number", selected_stop.getDriver_number());
        editor.putString("locality", selected_stop.getLocality());
        editor.putLong("latitude", Double.doubleToRawLongBits(selected_stop.getLatitude()));
        editor.putLong("longitude",Double.doubleToRawLongBits(selected_stop.getLongitude()));
        editor.apply();
    }


    public static void savePhoneNumberVisibilityStatus(Context context, boolean new_state){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_phone_number_visible", new_state);
        editor.apply();
    }

}
