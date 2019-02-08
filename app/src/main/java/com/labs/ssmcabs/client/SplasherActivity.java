package com.labs.ssmcabs.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplasherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splasher);

        if(isStopSetupComplete())
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplasherActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        else
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplasherActivity.this, SetupStopActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
    }


    private boolean isStopSetupComplete(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_setup_complete", false);
    }
}
