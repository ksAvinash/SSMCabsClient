package com.labs.ssmcabs.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.labs.ssmcabs.client.helper.CoordinateAdapter;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;
import com.labs.ssmcabs.client.helper.stopAdapter;

import java.util.ArrayList;
import java.util.List;

public class SetupStopActivity extends AppCompatActivity {

    private String TAG = "SET_STOP";
    ListView stop_list;
    List<stopAdapter> stopAdapterList = new ArrayList<>();
    Context context;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_stop);
        initializeViews();
        fetchAllStops();
    }


    private void initializeViews(){
        stop_list = findViewById(R.id.stop_list);
        context = SetupStopActivity.this;
        database = FirebaseDatabase.getInstance();
    }


    private void fetchAllStops(){
        final DatabaseReference stopsReference = database.getReference("stops");
        stopsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stopAdapterList.clear();
                Log.d(TAG, "All stops : "+dataSnapshot.getValue());

                for(DataSnapshot stop: dataSnapshot.getChildren()){
                    stopAdapter temp = stop.getValue(stopAdapter.class);
                    temp.setStop_name(stop.getKey());
                    stopAdapterList.add(temp);
                }
                displayStopList();
                stopsReference.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching database :"+ databaseError.toException());
                stopsReference.removeEventListener(this);

                Toast.makeText(SetupStopActivity.this, "Error fetching stops, try later", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
            }
        });
    }


    private void displayStopList(){
        ArrayAdapter<stopAdapter> adapter = new allStopsAdapterClass();
        stop_list.setAdapter(adapter);
        stop_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stopAdapter selected_stop = stopAdapterList.get(position);
                Log.d(TAG, "selected stop :"+selected_stop.getStop_name()+":"+selected_stop.getLocality()+":"+selected_stop.getDriver_number());

                subscribeToTopic(selected_stop.getStop_name());
                SharedPreferenceHelper.saveStopDetails(SetupStopActivity.this, selected_stop);
                fetchDriverDetails(selected_stop.getDriver_number());
                jumpToProfileActivity(convertStopName(selected_stop.getStop_name()));
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
                    SharedPreferenceHelper.saveDriverDetails(SetupStopActivity.this, adapter.getDriver_name(),
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




    private void jumpToProfileActivity(final String stop_name){
        Toast.makeText(context, "Selected "+stop_name, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SetupStopActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }



    private class allStopsAdapterClass extends ArrayAdapter<stopAdapter> {
        allStopsAdapterClass(){
            super(context, R.layout.stop_card_list_item, stopAdapterList);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                itemView = inflater.inflate(R.layout.stop_card_list_item, parent, false);
            }
            final stopAdapter current = stopAdapterList.get(position);


            TextView item_stop_name = itemView.findViewById(R.id.item_stop_name);
            item_stop_name.setText(convertStopName(current.getStop_name()));

            TextView item_locality = itemView.findViewById(R.id.item_locality);
            item_locality.setText(current.getLocality());

            return itemView;
        }
    }


    private void subscribeToTopic(final String stop_name){
        clearPreviousTopicSubscription();
        FirebaseMessaging.getInstance().subscribeToTopic(stop_name)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful())
                            Log.e(TAG, "error subscribing to topic : "+stop_name);
                        else
                            Log.i(TAG, "subscribed to topic : "+stop_name);
                    }
                });
    }


    private void clearPreviousTopicSubscription(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(SharedPreferenceHelper.fetchStopName(SetupStopActivity.this));
    }


    private String convertStopName(String stop_name){
        String[] words = stop_name.split("_");
        String res_name  = "";
        for(String word : words){
            res_name += word.substring(0, 1).toUpperCase()+word.substring(1)+" ";
        }
        return res_name;
    }
}
