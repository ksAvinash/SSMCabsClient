package com.labs.ssmcabs.client.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.labs.ssmcabs.client.MainActivity;
import com.labs.ssmcabs.client.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FB_MSG_SERVICE";

    private final int NOTIFICATION_ID = 11645;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "onMessageReceived : "+remoteMessage.getData());

        if (remoteMessage.getData().size() > 0) {
            createNotificationChannel(remoteMessage.getData().get("title"), remoteMessage.getData().get("status"));
            publishNotification(remoteMessage.getData().get("driver_name"), remoteMessage.getData().get("title"), remoteMessage.getData().get("topic_name"), remoteMessage.getData().get("status"), remoteMessage.getData().get("distance"));
        }
    }


    private void publishNotification(String driver_name, String title, String topic_name, String status, String distance){
        Log.i(TAG, "received notification "+status);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = null;

        if(status.equals("1"))
            notificationBuilder = new NotificationCompat.Builder(this, "TRIP_ALERT_1");
        else if(status.equals("2"))
            notificationBuilder = new NotificationCompat.Builder(this, "TRIP_ALERT_2");

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
                .addAction(R.drawable.notification_icon, "track cab", pendingIntent)
                .setContentInfo("Info")
                .setContentIntent(pendingIntent);

        if (status.equals("1"))
            notificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.one_km_tone));
        else if(status.equals("2"))
            notificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.two_km_tone));

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }



    private void createNotificationChannel(String title, String status){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(status.equals("1") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel("TRIP_ALERT_1",
                    "The driver has started the trip",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(title);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.one_km_tone), attributes);
            notificationManager.createNotificationChannel(channel);

        }else if(status.equals("2") &&  Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel("TRIP_ALERT_2",
                    "The driver has started the trip",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(title);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.two_km_tone), attributes);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
