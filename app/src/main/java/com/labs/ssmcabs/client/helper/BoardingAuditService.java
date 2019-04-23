package com.labs.ssmcabs.client.helper;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

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
                CommonHelper.updateBoardingAudit(this);
                break;

            case ACTION_NOT_BOARDING:
                break;
        }
    }
}