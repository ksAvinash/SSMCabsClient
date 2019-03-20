package com.labs.ssmcabs.client.helper;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class BoardingAuditService extends IntentService {

    public BoardingAuditService() {
        super(BoardingAuditService.class.getSimpleName());
    }
    private static final int NOTIFICATION_ID = 789532;
    public static final String ACTION_BOARDING = "Boarding";
    public static final String ACTION_NOT_BOARDING = "Not Boarding";

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.i("BOARDING_SERVICE", action);
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);

        switch(action){
            case ACTION_BOARDING:
                updateBoardingAudit();
                break;

            case ACTION_NOT_BOARDING:
                break;
        }
    }


    private void updateBoardingAudit(){
        if(SharedPreferenceHelper.isUserSetupComplete(this)){
            Date date = new Date();
            String stop_name = SharedPreferenceHelper.fetchStopName(this);
            String username = SharedPreferenceHelper.fetchUserName(this);
            String phone_number = SharedPreferenceHelper.fetchUserPhoneNumber(this);
            String company_code = SharedPreferenceHelper.fetchCompanyCode(this);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            DatabaseReference boardingCompanyAuditRef = database.getReference("boarding_audits/company_codes/"+company_code+"/"+stop_name+"/"+date_formatter.format(date)+"/"+username);
            DatabaseReference boardingStopAuditRef = database.getReference("boarding_audits/stops/"+stop_name+"/"+date_formatter.format(date)+"/"+username);

            if(SharedPreferenceHelper.fetchPhoneNumberVisibilityStatus(this)){
                boardingCompanyAuditRef.setValue(phone_number);
                boardingStopAuditRef.setValue(phone_number);
            }else{
                boardingCompanyAuditRef.setValue("0000000000");
                boardingStopAuditRef.setValue("0000000000");
            }
        }
    }
}