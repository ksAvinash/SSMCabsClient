package com.labs.ssmcabs.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {

    EditText user_name, user_number;
    TextView stop_name;
    Button profile_submit_button;
    FirebaseDatabase database;

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
        stop_name = findViewById(R.id.stop_name);

        user_name.setText(SharedPreferenceHelper.fetchUserName(ProfileActivity.this));
        user_number.setText(SharedPreferenceHelper.fetchUserPhoneNumber(ProfileActivity.this));
        stop_name.setText(convertStopName(SharedPreferenceHelper.fetchStopName(ProfileActivity.this)));
        stop_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPreviousTopicSubscription();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ProfileActivity.this, SetupStopActivity.class);
                        startActivity(intent);
                        finish();
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

                if(name.length() <3 || name.length() > 22 || !isUserNameValid(name)){
                    profile_submit_button.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Invalid user name!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else{
                    if(validateNumber(phone)){
                        pushUserToStop(name, phone);
                        saveProfileAndJumpToMainActivity(name, phone);
                    }else{
                        profile_submit_button.setEnabled(true);
                        Snackbar.make(findViewById(android.R.id.content), "Invalid phone number!", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }

                }
            }
        });
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


    private void saveProfileAndJumpToMainActivity(String name, String phone){
        Snackbar.make(findViewById(android.R.id.content), "Profile Update successful", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        SharedPreferenceHelper.saveUserProfileDetails(ProfileActivity.this, name, phone);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);

    }

    private String convertStopName(String stop_name){
        String[] words = stop_name.split("_");
        String res_name  = "";
        for(String word : words){
            res_name += word.substring(0, 1).toUpperCase()+word.substring(1)+" ";
        }
        return res_name;
    }

    private void clearPreviousTopicSubscription(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(SharedPreferenceHelper.fetchStopName(ProfileActivity.this));
    }



}
