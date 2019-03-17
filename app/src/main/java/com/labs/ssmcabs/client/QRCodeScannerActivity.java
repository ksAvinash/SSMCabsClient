package com.labs.ssmcabs.client;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.labs.ssmcabs.client.helper.BackendResponseCallback;
import com.labs.ssmcabs.client.helper.HttpHelper;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, BackendResponseCallback {
    private ZXingScannerView mScannerView;

    private final String TAG = "QR_SCAN";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
        initializeViews();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.startCamera();
        mScannerView.setAutoFocus(true);
        mScannerView.setResultHandler(this);
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.i(TAG, "qr_message : "+rawResult);
        verifyQRCode(rawResult.toString());
    }

    private void verifyQRCode(String code){
        startProgressDialog("Authenticating QR code please wait..");
        new HttpHelper.VerifyQRCodeTask(QRCodeScannerActivity.this).execute(code);
    }

    private void initializeViews(){
        mScannerView = findViewById(R.id.scanner_fragment);
        progressDialog = new ProgressDialog(QRCodeScannerActivity.this);
    }

    private void startProgressDialog(String message){
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void stopProgressDialog(){
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onTaskDone(Object... values) {
        boolean isQRValid = (boolean) values[0];
        stopProgressDialog();

        if(isQRValid){
            updateBoardedTime();
            Snackbar.make(findViewById(android.R.id.content), "Boarded successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        }
        else{
            String reason = (String) values[1];
            Snackbar.make(findViewById(android.R.id.content), reason, Snackbar.LENGTH_LONG).show();
            mScannerView.resumeCameraPreview(QRCodeScannerActivity.this);
        }
    }

    private void updateBoardedTime(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final Date date = new Date();
        final SimpleDateFormat month_formatter = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        final SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd a", Locale.getDefault());

        final DatabaseReference userBoardLogRef = database.getReference("user_board_logs/"+SharedPreferenceHelper.fetchCompanyCode(QRCodeScannerActivity.this)+"/user_logs/"+SharedPreferenceHelper.fetchUserPhoneNumber(QRCodeScannerActivity.this)+"/"+
                month_formatter.format(date)+"/"+date_formatter.format(date)+"/");
        final DatabaseReference monthBoardLogRef = database.getReference("user_board_logs/"+SharedPreferenceHelper.fetchCompanyCode(QRCodeScannerActivity.this)+"/month_logs/"+month_formatter.format(date)+"/"+date_formatter.format(date)+"/"
                +SharedPreferenceHelper.fetchUserPhoneNumber(QRCodeScannerActivity.this));
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
}
