package com.labs.ssmcabs.client.helper;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonHelper {

    public static void updateBoardedTime(Context context){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final Date date = new Date();
        String phoneNumber = SharedPreferenceHelper.fetchUserPhoneNumber(context);
        String company_code = SharedPreferenceHelper.fetchCompanyCode(context);
        final SimpleDateFormat month_formatter = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        final SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd a", Locale.getDefault());

        final DatabaseReference userBoardLogRef = database.getReference("/boarding_data/user_board_logs/"+company_code+"/user_logs/"+phoneNumber+"/"+
                month_formatter.format(date)+"/"+date_formatter.format(date));
        final DatabaseReference monthBoardLogRef = database.getReference("/boarding_data/user_board_logs/"+company_code+"/company_logs/"+month_formatter.format(date)+"/"+date_formatter.format(date)+"/"
                +phoneNumber);
        userBoardLogRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userBoardLogRef.removeEventListener(this);

                if(dataSnapshot.getValue() == null){
                    SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a", Locale.getDefault());
                    userBoardLogRef.setValue(time_formatter.format(date));
                    monthBoardLogRef.setValue(time_formatter.format(date));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userBoardLogRef.removeEventListener(this);
            }
        });
    }


    public static void updateBoardingAudit(Context context){
        if(SharedPreferenceHelper.isUserSetupComplete(context)){
            Date date = new Date();
            String stop_name = SharedPreferenceHelper.fetchStopName(context);
            String username = SharedPreferenceHelper.fetchUserName(context);
            String phone_number = SharedPreferenceHelper.fetchUserPhoneNumber(context);
            String company_code = SharedPreferenceHelper.fetchCompanyCode(context);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            DatabaseReference boardingCompanyAuditRef = database.getReference("boarding_audits/company_codes/"+company_code+"/"+stop_name+"/"+date_formatter.format(date)+"/"+username);
            DatabaseReference boardingStopAuditRef = database.getReference("boarding_audits/stops/"+stop_name+"/"+date_formatter.format(date)+"/"+username);

            if(SharedPreferenceHelper.fetchPhoneNumberVisibilityStatus(context)){
                boardingCompanyAuditRef.setValue(phone_number);
                boardingStopAuditRef.setValue(phone_number);
            }else{
                boardingCompanyAuditRef.setValue("0000000000");
                boardingStopAuditRef.setValue("0000000000");
            }
        }
    }


}
