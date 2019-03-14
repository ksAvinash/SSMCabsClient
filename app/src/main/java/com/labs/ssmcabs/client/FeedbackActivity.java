package com.labs.ssmcabs.client;

import android.content.Context;
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
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);


        final EditText feedback_edittext = findViewById(R.id.feedback_edittext);
        Button feedback_submit_button = findViewById(R.id.feedback_submit_button);
        feedback_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(feedback_edittext.getWindowToken(), 0);

                String feedback = feedback_edittext.getText().toString();
                feedback_edittext.setEnabled(false);
                if(feedback.length() > 0){
                    pushFeedbackToBackend(feedback);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }else{
                    feedback_edittext.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Please report any issue or feedback", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }



    private void pushFeedbackToBackend(String feedback){
        Snackbar.make(findViewById(android.R.id.content), "Thanks for your feedback or issue report!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("feedback/"+SharedPreferenceHelper.fetchCompanyCode(FeedbackActivity.this)+"/"+
                SharedPreferenceHelper.fetchUserPhoneNumber(FeedbackActivity.this)+"/"+formatter.format(date));
        reference.setValue(feedback);
    }
}
