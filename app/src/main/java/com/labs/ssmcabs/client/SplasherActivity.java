package com.labs.ssmcabs.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labs.ssmcabs.client.helper.BoardingConfigurationAdapter;
import com.labs.ssmcabs.client.helper.CoordinateAdapter;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class SplasherActivity extends AppCompatActivity {

    FirebaseDatabase database;
    private final String TAG = "SPLASHER_CLIENT";
    BoardingConfigurationAdapter boardingConfigurationAdapter;
    Timer timer_15;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splasher);
        MobileAds.initialize(this, "ca-app-pub-9681985190789334~7745401881");

        database = FirebaseDatabase.getInstance();

        if(SharedPreferenceHelper.isStopSetupComplete(SplasherActivity.this)){
            updateStopDriverNumber();

            timer_15 = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    timer_15.cancel();
                    if (boardingConfigurationAdapter == null) {
                        SplasherActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                killActivity("No response from server, please try later");
                            }
                        });
                    }
                }
            };
            timer_15.schedule(timerTask, 15000L);
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


    private void updateStopDriverNumber(){
        final String stop_name = SharedPreferenceHelper.fetchStopName(this);

        final DatabaseReference myStopRef = database.getReference("stops/"+stop_name+"/driver_number");
        myStopRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myStopRef.removeEventListener(this);
                Log.v(TAG, "MY_STOP : "+dataSnapshot.getValue());

                String driver_number = (String) dataSnapshot.getValue();
                SharedPreferenceHelper.saveDriverNumber(SplasherActivity.this, stop_name, driver_number);
                fetchDriverDetails(driver_number);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                myStopRef.removeEventListener(this);
                Log.w(TAG, "MY_STOP : Error fetching stop details");
            }
        });
    }


    private void fetchDriverDetails(final String driver_number){
        final DatabaseReference driverDetailsRef = database.getReference("drivers/"+driver_number);
        driverDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v(TAG, "STOP_DRIVER : "+dataSnapshot.getValue());
                driverDetailsRef.removeEventListener(this);
                CoordinateAdapter adapter = dataSnapshot.getValue(CoordinateAdapter.class);
                if(adapter != null) {
                    SharedPreferenceHelper.saveDriverDetails(SplasherActivity.this, adapter.getDriver_name(),
                            adapter.getVehicle_number(), adapter.getVehicle_type());
                    fetchBoardingConfiguration();
                }else{
                    Log.e(TAG, "STOP_DRIVER : null value");
                    killActivity("Error fetching driver details, contact Admin!");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                driverDetailsRef.removeEventListener(this);
                Log.w(TAG, "STOP_DRIVER : Error fetching stop driver details");
                killActivity("Error fetching driver details, contact Admin!");
            }
        });
    }

    private void fetchBoardingConfiguration(){
        final DatabaseReference boardingConfigRef = database.getReference("boarding_configuration");
        boardingConfigRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boardingConfigRef.removeEventListener(this);
                Log.v(TAG, "BOARDING_CONFIG : "+dataSnapshot.getValue());
                boardingConfigurationAdapter = dataSnapshot.getValue(BoardingConfigurationAdapter.class);
                if(boardingConfigurationAdapter != null){
                    SharedPreferenceHelper.saveBoardingConfiguration(SplasherActivity.this, boardingConfigurationAdapter);
                    jumpToMainActivity();
                }else {
                    Log.w(TAG, "BOARDING_CONFIG : null value");
                    killActivity("Error fetching configuration, contact Admin!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                boardingConfigRef.removeEventListener(this);
                Log.e(TAG, "BOARDING_CONFIG : error fetching boarding configuration");
                killActivity("Error fetching configuration, contact Admin!");
            }
        });
    }



    private void jumpToMainActivity(){
        timer_15.cancel();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplasherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }



    private void killActivity(String message){
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2500);
    }
}
