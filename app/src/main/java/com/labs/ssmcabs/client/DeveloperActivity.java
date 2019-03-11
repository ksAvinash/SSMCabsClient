package com.labs.ssmcabs.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DeveloperActivity extends AppCompatActivity {

    ImageView dev_icon;
    TextView dev_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        dev_email = findViewById(R.id.dev_email);
        dev_icon = findViewById(R.id.dev_icon);

        dev_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailIntent();
            }
        });

        dev_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailIntent();
            }
        });
    }



    private void sendEmailIntent(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","solutionsaccelerated@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SSM Cabs query");
        startActivity(Intent.createChooser(emailIntent, "SSM cabs query"));
    }

}
