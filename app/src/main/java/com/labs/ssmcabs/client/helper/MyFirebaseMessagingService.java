package com.labs.ssmcabs.client.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.labs.ssmcabs.client.R;
import com.labs.ssmcabs.client.SplasherActivity;

import java.util.Calendar;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FB_MSG_SERVICE";

    private static final int NOTIFICATION_ID = 789232;
    public static final String ACTION_BOARDED = "action_boarded";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "onMessageReceived : "+remoteMessage.getData());

        if (remoteMessage.getData().size() > 0) {
            createNotificationChannel();
            publishNotification(remoteMessage.getData().get("driver_name"), remoteMessage.getData().get("title"), remoteMessage.getData().get("topic_name"), remoteMessage.getData().get("status"), remoteMessage.getData().get("distance"));
        }
    }


    private void publishNotification(String driver_name, String title, String topic_name, String status, String distance){

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        boolean is_am = calendar.get(Calendar.AM_PM) == Calendar.AM;


        Log.i(TAG, "received notification "+status);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, SplasherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = null;

        switch (status) {
            case "0":
                if(is_am)
                    notificationBuilder = new NotificationCompat.Builder(this, "cab arrived alert");
                else
                    notificationBuilder = new NotificationCompat.Builder(this, "stop arrived alert");
                break;
            case "1":
                notificationBuilder = new NotificationCompat.Builder(this, "1km away alert");
                break;
            case "2":
                notificationBuilder = new NotificationCompat.Builder(this, "2km away alert");
                break;
        }

        notificationBuilder
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.notification_icon)
                .setTicker(driver_name)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentText(topic_name)
                .setContentInfo("Info")
                .setContentIntent(pendingIntent);

        switch (status) {
            case "0":
                Intent boardedIntent = new Intent(this, UpdateBoardedTimeService.class)
                        .setAction(ACTION_BOARDED);
                PendingIntent boardedPendingIntent = PendingIntent.getService(this, 275,
                        boardedIntent, PendingIntent.FLAG_ONE_SHOT);
                if(is_am){
                    notificationBuilder.addAction(R.drawable.notification_icon, "track cab", pendingIntent);
                    notificationBuilder.addAction(R.drawable.notification_icon, "boarded", boardedPendingIntent);
                    notificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cab_arrived));
                }else{
                    notificationBuilder.setContentTitle("Cab has reached your stop");
                    notificationBuilder.setContentIntent(boardedPendingIntent);
                    notificationBuilder.addAction(R.drawable.notification_icon, "reached stop", boardedPendingIntent);
                    notificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stop_arrived));
                }
                break;
            case "1":
                notificationBuilder.addAction(R.drawable.notification_icon, "track cab", pendingIntent);
                notificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.one_km_tone));
                break;
            case "2":
                notificationBuilder.addAction(R.drawable.notification_icon, "track cab", pendingIntent);
                notificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.two_km_tone));
                break;
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }



    private void createNotificationChannel(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel("cab arrived alert",
                    "Cab arrived!",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("cab arrived notification");
            channel.setLightColor(Color.RED);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cab_arrived), attributes);
            notificationManager.createNotificationChannel(channel);

            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel = new NotificationChannel("stop arrived alert",
                    "Cab has reached your stop",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.BLUE);
            channel.setDescription("stop arrived alert notification");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stop_arrived), attributes);
            notificationManager.createNotificationChannel(channel);


            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel = new NotificationChannel("1km away alert",
                    "Cab is 1km away",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.BLUE);
            channel.setDescription("1km alert notification");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.one_km_tone), attributes);
            notificationManager.createNotificationChannel(channel);

            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel = new NotificationChannel("2km away alert",
                    "Cab is 2km away",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("2km alert notification");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.two_km_tone), attributes);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
