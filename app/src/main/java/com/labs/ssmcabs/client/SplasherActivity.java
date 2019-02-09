package com.labs.ssmcabs.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labs.ssmcabs.client.helper.stopAdapter;

public class SplasherActivity extends AppCompatActivity {

    FirebaseDatabase database;
    private final String TAG = "SPLASHER_CLIENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splasher);

        database = FirebaseDatabase.getInstance();



        if(isStopSetupComplete()){
            updateStopDetails();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplasherActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplasherActivity.this, SetupStopActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
        }


    }


    private boolean isStopSetupComplete(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_setup_complete", false);
    }


    private void updateStopDetails(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        final String stop_name = sharedPreferences.getString("stop_name", "");

        final DatabaseReference myStopRef = database.getReference("stops/"+stop_name);

        myStopRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v(TAG, "MY_STOP : "+dataSnapshot.getValue());
                stopAdapter adapter = dataSnapshot.getValue(stopAdapter.class);
                adapter.setStop_name(stop_name);
                saveStopDetails(adapter);
                myStopRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                myStopRef.removeEventListener(this);
                Log.w(TAG, "MY_STOP : Error fetching stop details");
            }
        });
    }


    private void saveStopDetails(stopAdapter adapter){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("stop_name", adapter.getStop_name());
        editor.putString("driver_name", adapter.getDriver_name());
        editor.putString("driver_number", adapter.getDriver_number());
        editor.putString("vehicle_number", adapter.getVehicle_number());
        editor.putString("vehicle_type", adapter.getVehicle_type());
        editor.putLong("latitude", Double.doubleToRawLongBits(adapter.getLatitude()));
        editor.putLong("longitude",Double.doubleToRawLongBits(adapter.getLongitude()));
        editor.apply();
    }
}
