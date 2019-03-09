package com.labs.ssmcabs.client.helper;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class UpdateBoardedTimeService extends IntentService {

    public UpdateBoardedTimeService() {
        super(UpdateBoardedTimeService.class.getSimpleName());
    }
    public static final String ACTION_BOARDED = "action_boarded";
    private static final int NOTIFICATION_ID = 789232;

    private void updateBoardedTime(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final Date date = new Date();
        final SimpleDateFormat month_formatter = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        final SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd a", Locale.getDefault());


        final DatabaseReference userBoardLogRef = database.getReference("user_board_logs/"+SharedPreferenceHelper.fetchCompanyCode(UpdateBoardedTimeService.this)+"/"+SharedPreferenceHelper.fetchUserPhoneNumber(UpdateBoardedTimeService.this)+"/"+
                month_formatter.format(date)+"/"+date_formatter.format(date)+"/");
        userBoardLogRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userBoardLogRef.removeEventListener(this);

                if(dataSnapshot.getValue() == null){
                    SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a", Locale.getDefault());
                    userBoardLogRef.setValue(time_formatter.format(date));
                    SharedPreferenceHelper.saveLastBoardTime(UpdateBoardedTimeService.this, time_formatter.format(date));
                    Log.i("BOARD_TIME", date_formatter.format(date));
                }else{
                    Log.i("BOARD_TIME", "Cab already boarded today");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userBoardLogRef.removeEventListener(this);
            }
        });




    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.i("UPDATE_BOARD_TIME", action);

        if (Objects.equals(action, ACTION_BOARDED)){
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
            updateBoardedTime();
        }
    }
}
