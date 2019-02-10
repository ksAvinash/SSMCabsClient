package com.labs.ssmcabs.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labs.ssmcabs.client.helper.CoordinateAdapter;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

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


        if(SharedPreferenceHelper.isStopSetupComplete(SplasherActivity.this) && SharedPreferenceHelper.isLastBoardDateValid(SplasherActivity.this)){
            updateStopDriverNumber();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplasherActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
        }else{

            Snackbar.make(findViewById(android.R.id.content), "Last cab boarding was more than 3 days ago!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            removeUserFromStop();

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


    private void removeUserFromStop(){
        DatabaseReference myRef = database.getReference("stops/"+ SharedPreferenceHelper.fetchStopName(SplasherActivity.this)
                +"/users/"+SharedPreferenceHelper.fetchUserName(SplasherActivity.this));
        myRef.removeValue();
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
                Log.v(TAG, "MY_STOP : "+dataSnapshot.getValue());
                driverDetailsRef.removeEventListener(this);
                CoordinateAdapter adapter = dataSnapshot.getValue(CoordinateAdapter.class);
                if(adapter != null) {
                    SharedPreferenceHelper.saveDriverDetails(SplasherActivity.this, adapter.getDriver_name(),
                            adapter.getVehicle_number(), adapter.getVehicle_type());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                driverDetailsRef.removeEventListener(this);
                Log.w(TAG, "MY_STOP : Error fetching stop details");
            }
        });
    }
}
