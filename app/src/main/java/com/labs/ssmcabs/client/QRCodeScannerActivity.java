package com.labs.ssmcabs.client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.labs.ssmcabs.client.helper.BackendResponseCallback;
import com.labs.ssmcabs.client.helper.CommonHelper;
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
    InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
        initializeViews();
        loadNewAd();
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
        finish();
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
            CommonHelper.updateBoardedTime(QRCodeScannerActivity.this);
            displaySuccessAlertDialog();
        }
        else{
            String reason = (String) values[1];
            Snackbar.make(findViewById(android.R.id.content), reason, Snackbar.LENGTH_LONG).show();
            mScannerView.resumeCameraPreview(QRCodeScannerActivity.this);
        }
    }

    private void displaySuccessAlertDialog(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(QRCodeScannerActivity.this);
        builder1.setMessage("Boarding successful");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        showAd();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void loadNewAd(){
        mInterstitialAd = new InterstitialAd(QRCodeScannerActivity.this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                finish();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                finish();
            }

            @Override
            public void onAdClosed() {
                finish();
            }
        });
    }

    private void showAd(){
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.w("FB_ADS", "The interstitial wasn't loaded yet.");
        }
    }
}
