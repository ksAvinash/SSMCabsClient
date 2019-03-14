package com.labs.ssmcabs.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    EditText user_name, user_number, company_code;
    TextView stop_name;
    Button profile_submit_button;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private final String TAG = "SIGN_UP";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
    }


    private void initializeViews(){
        database = FirebaseDatabase.getInstance();
        Snackbar.make(findViewById(android.R.id.content), "Click stop name to change!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        user_name = findViewById(R.id.user_name);
        user_number = findViewById(R.id.user_number);
        company_code = findViewById(R.id.company_code);
        stop_name = findViewById(R.id.stop_name);
        progressDialog = new ProgressDialog(ProfileActivity.this);

        user_name.setText(SharedPreferenceHelper.fetchUserName(ProfileActivity.this));
        user_number.setText(SharedPreferenceHelper.fetchUserPhoneNumber(ProfileActivity.this));
        stop_name.setText(SharedPreferenceHelper.fetchConvertedStopName(ProfileActivity.this));
        company_code.setText(SharedPreferenceHelper.fetchCompanyCode(ProfileActivity.this));

        stop_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPreviousTopicSubscription();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ProfileActivity.this, SetupStopActivity.class);
                        startActivity(intent);
                    }
                }, 200);
            }
        });


        profile_submit_button = findViewById(R.id.profile_submit_button);
        profile_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_submit_button.setEnabled(false);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(user_name.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(user_number.getWindowToken(), 0);

                String name = user_name.getText().toString();
                String phone = user_number.getText().toString();
                String code = company_code.getText().toString();

                if(name.length() <3 || name.length() > 22 || !isUserNameValid(name)){
                    profile_submit_button.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Invalid user name!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else if(!validateNumber(phone)){
                    profile_submit_button.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Invalid phone number!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else if(!isCompanyCodeValid(code)){
                    profile_submit_button.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Please enter the 4 digit company ID", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    progressDialog.setMessage("Checking Company code please wait..");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    verifyCompanyCode(code, name, phone);
                }
            }
        });
    }

    private void verifyCompanyCode(final String code, final String name, final String phone){
        final DatabaseReference companyCodeRef = database.getReference("company_codes/"+code);
        companyCodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                companyCodeRef.removeEventListener(this);
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                if(dataSnapshot.exists()){
                    pushUserToStop(name, phone);
                    saveProfileAndJumpToMainActivity(name, phone, code);
                }else{
                    Snackbar.make(findViewById(android.R.id.content), "Invalid company code, contact admin", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    profile_submit_button.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                companyCodeRef.removeEventListener(this);
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                profile_submit_button.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content), "Something wrong happened, contact admin", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.e(TAG, "Invalid company code entered");
            }
        });
    }

    private boolean isCompanyCodeValid(String code){
        return code.matches("[0-9]{4}");
    }

    private boolean validateNumber(String phone_number){
        return phone_number.matches("[0-9]{10}");
    }

    private boolean isUserNameValid(String user_name){
       return user_name.matches("[a-zA-z][a-zA-Z ]*");
    }

    private void pushUserToStop(String name, String phone){
        clearPreviousProfileDetails();

        DatabaseReference myRef = database.getReference("stops/"+ SharedPreferenceHelper.fetchStopName(this) +"/users/"+name);
        myRef.setValue(phone);
    }

    private void clearPreviousProfileDetails(){
        String username = SharedPreferenceHelper.fetchUserName(ProfileActivity.this);
        if(!username.equals("")){
            DatabaseReference myRef = database.getReference("stops/"+ SharedPreferenceHelper.fetchStopName(this) +"/users/"+username);
            myRef.removeValue();
        }
    }


    private void saveProfileAndJumpToMainActivity(String name, String phone, String code){
        Snackbar.make(findViewById(android.R.id.content), "Profile Update successful", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        Date date = new Date();
        SharedPreferenceHelper.saveUserProfileDetails(ProfileActivity.this, name, phone, code);
        DatabaseReference signupRef = database.getReference("user_signup/"+ code+"/"+phone);
        SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        signupRef.setValue(date_formatter.format(date));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);

    }

    private void clearPreviousTopicSubscription(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(SharedPreferenceHelper.fetchStopName(ProfileActivity.this));
    }



}
