package com.metzukan.club;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Utils {
    public static final String METZUKAN_CHANNEL_ID = "com.metzukan.club";
    public static final String METZUKAN_OK_ACTION = "metzukan_ok";
    public static final int SUBMIT_OK_DURATION_MS = 1000 * 60;
    public static final int STATUS_CHECK_INTERVAL_MS = 1000 * 5;

    private static final String METZUKAN_CACHE_FILE_NAME = "metzukan.data";
    private static Vibrator vibrator = null;


    public static void WriteCache(Context context, String data) {
        FileOutputStream fOut = null;
        try {
            fOut = context.openFileOutput(METZUKAN_CACHE_FILE_NAME, context.MODE_PRIVATE);
            fOut.write(data.getBytes());
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String ReadCache(Context context) {
        String temp = "";

        try {
            FileInputStream fin = context.openFileInput(METZUKAN_CACHE_FILE_NAME);
            int c;
            while ((c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
        } catch (Exception e) {
            temp = "";
        }

        return temp;
    }

    public static void ShowMetzukanNotification(Context context) {
        Intent action1Intent = new Intent(context, NotificationService.class)
                .setAction(METZUKAN_OK_ACTION);

        PendingIntent action1PendingIntent = PendingIntent.getService(context, 0,
                action1Intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, METZUKAN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(context.getString(R.string.metzukan_notification_title))
                .setContentText(context.getString(R.string.metzukan_notification_msg))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(action1PendingIntent)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.metzukan_notification_submit_ok),
                        action1PendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(10, builder.build());
    }

    public static void startVibrate(Context context){
        if(vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        }
        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.EFFECT_DOUBLE_CLICK));
        } else {
            vibrator.vibrate(pattern, 3);
        }
    }

    public static void stopVibrate(Context context){
        if(vibrator != null) {
            vibrator.cancel();
        }
    }
}
