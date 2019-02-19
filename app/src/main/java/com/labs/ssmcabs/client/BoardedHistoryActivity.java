package com.labs.ssmcabs.client;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BoardedHistoryActivity extends AppCompatActivity {

    TextView boarded_count;
    FirebaseDatabase database;
    DatabaseReference boardRef;
    Date curr_month;
    private final String TAG = "BOARDED_ACTIVITY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarded_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();

        fetchBoardedActivitiesOfCurrentMonth();
    }

    private void initializeViews(){
        boarded_count = findViewById(R.id.boarded_count);
        database = FirebaseDatabase.getInstance();
        curr_month = new Date();
        SimpleDateFormat month_formatter = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        boardRef = database.getReference("user_board_logs/"+ SharedPreferenceHelper.fetchUserPhoneNumber(BoardedHistoryActivity.this)
                    +"/"+month_formatter.format(curr_month));
    }




    private void fetchBoardedActivitiesOfCurrentMonth(){
        boardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boardRef.removeEventListener(this);
                boarded_count.setText(dataSnapshot.getChildrenCount()+" Activites");


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                boardRef.removeEventListener(this);
                Log.w(TAG, "error fetching boarded logs for user");
            }
        });
    }
}
