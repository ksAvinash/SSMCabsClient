package com.labs.ssmcabs.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    EditText user_name, user_number;
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

        user_name = findViewById(R.id.user_name);
        user_number = findViewById(R.id.user_number);

        profile_submit_button = findViewById(R.id.profile_submit_button);
        profile_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(user_name.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(user_number.getWindowToken(), 0);

                profile_submit_button.setEnabled(false);

                String name = user_name.getText().toString();
                String phone = user_number.getText().toString();
                if(name.length() == 0){
                    profile_submit_button.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Invalid user name!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else{
                    if(!validateNumber(phone))
                        phone = "0000000000";
                    pushUserToStop(name, phone);
                    saveProfileAndJumpToMainActivity(name, phone);
                }
            }
        });
    }


    private boolean validateNumber(String phone_number){
        return phone_number.matches("[0-9]{10}");
    }


    private String fetchStopName(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("stop_name", "");
    }


    private void pushUserToStop(String name, String phone){
        DatabaseReference myRef = database.getReference("stops/"+fetchStopName()+"/users/"+name);
        myRef.setValue(phone);
    }

    private void saveProfileAndJumpToMainActivity(String name, String phone){
        Snackbar.make(findViewById(android.R.id.content), "Profile Update successful", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("phone_number", phone);
        editor.putBoolean("is_setup_complete", true);
        editor.apply();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);

    }

}
