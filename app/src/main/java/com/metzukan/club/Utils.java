package com.metzukan.club;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Utils {
    public static final String BASE_UEL = "https://metzukan.herokuapp.com";

    public static final String METZUKAN_CHANNEL_ID = "com.metzukan.club";
    public static final String METZUKAN_OK_ACTION = "metzukan_ok";
    public static final Integer METZUKAN_NOTIFICATION_BACKGROUND_SERVICE_ID = 1;
    public static final Integer METZUKAN_NOTIFICATION_ID = 10;
    public static final int STATUS_CHECK_INTERVAL_MS = 1000 * 5; // 5 sec
    public static final int PROGRESS_CHECK_INTERVAL_MS = 1000; // 1 sec
    public static final Integer REQUEST_TIMEOUT_MS = 1000 * 30; // 30 sec
    public static final Integer TIME_TO_SEND_NOT_RESPONDING_ACK_MS = 1000 * 60 * 1; // 1000 * 60 * 60 * 4; // 4 hours
    public static final Integer TIME_TO_SEND_EMERGENCY_ACK_MS = 1000 * 90 * 1; // 1000 * 60 * 60 * 8; // 8 hours

    // Cache files
    private static final String METZUKAN_CACHE_FILE_NAME = "metzukan.data";
    private static final String METZUKAN_CACHE_JWT_SECRETE = "metzukan.secrete";
    private static final String METZUKAN_CACHE_INTERVAL = "metzukan.interval";
    private static final String METZUKAN_CACHE_LAST_SUBMIT = "metzukan.submit";

    private static String _jwt_secrete = "";
    private static long _ackIntervalMs = 999999999;
    private static Context _context = null;
    private static Vibrator _vibrator = null;
    private static Integer _infoNotificationNextId = METZUKAN_NOTIFICATION_ID + 1;

    public static void LoadMainContext(Context context) {
        _context = context;
        try {
            // On boot, read the jwt and ack interval from the saved cache file
            _jwt_secrete = ReadCache(METZUKAN_CACHE_JWT_SECRETE);
            _ackIntervalMs = Long.parseLong(ReadCache(METZUKAN_CACHE_INTERVAL));
        } catch (Exception e) {
        }
    }

    /**
     * Get the session sign JWT secrete
     * @return The session JWT string, or empty string if not exists
     */
    public static String GetJwtSecrete() {
        return _jwt_secrete;
    }

    /**
     * Set the session JWT
     * @param jwtSecrete The session JWT string, or empty string in order to remove the session
     */
    public static void SetJwtSecrete(String jwtSecrete) {
        WriteCache(METZUKAN_CACHE_JWT_SECRETE, jwtSecrete);
        _jwt_secrete = jwtSecrete;
    }

    /**
     * Get the time to ack (means, the time that user select as time to alarm if not responding)
     * @return The interval ack as ms
     */
    public static long GetAckIntervalMs() {
        return _ackIntervalMs;
    }

    /**
     * Set the user selection ack
     * @param ackIntervalMs the interval ack ms duration as ms
     */
    public static void SetAckIntervalMs(Integer ackIntervalMs) {
        _ackIntervalMs = ackIntervalMs;
        WriteCache(METZUKAN_CACHE_INTERVAL, ackIntervalMs.toString());
    }

    /**
     * Get the last submit that saved to a cache file
     * @return The last submit time as UTC
     */
    public static long GetLastSubmitUTCCache() {
        try {
            return Long.parseLong(ReadCache(METZUKAN_CACHE_LAST_SUBMIT));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Set the last submit, so the value will kept in a cache file
     * @param lastSubmitUTC The last submit time as UTC
     */
    public static void SetLastSubmitCache(long lastSubmitUTC) {
        WriteCache(METZUKAN_CACHE_LAST_SUBMIT, lastSubmitUTC + "");
    }

    /**
     * Show the metzukan alert notification
     */
    public static void ShowMetzukanNotification() {
        Intent action1Intent = new Intent(_context, NotificationService.class)
                .setAction(METZUKAN_OK_ACTION);

        PendingIntent action1PendingIntent = PendingIntent.getService(_context, 0,
                action1Intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, METZUKAN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(_context.getString(R.string.metzukan_notification_title))
                .setContentText(_context.getString(R.string.metzukan_notification_msg))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(action1PendingIntent)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_launcher_foreground, _context.getString(R.string.metzukan_notification_submit_ok),
                        action1PendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(METZUKAN_NOTIFICATION_ID, builder.build());
    }

    /**
     * Show the metzukan alert notification
     */
    public static void HideMetzukanNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(METZUKAN_NOTIFICATION_ID);
    }

    /**
     * Show an info notification (about any action)
     */
    public static void ShowInfoNotification(int messageId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, METZUKAN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(_context.getString(R.string.metzukan_notification_title))
                .setContentText(_context.getString(messageId))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(false);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(_infoNotificationNextId++, builder.build());
    }
    /**
     * Start the phone vibration
     */
    public static void startVibrate(){

        if(_vibrator == null) {
            _vibrator = (Vibrator) _context.getSystemService(_context.VIBRATOR_SERVICE);
        }
        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};

        if (Build.VERSION.SDK_INT >= 26) {
            _vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.EFFECT_DOUBLE_CLICK));
        } else {
            _vibrator.vibrate(pattern, 3);
        }
    }

    /**
     * Stop the phone vibration
     */
    public static void stopVibrate(){
        if(_vibrator != null) {
            _vibrator.cancel();
        }
    }

    /**
     * Show a toast of message
     * @param stringId The string ID
     */
    public static void ShowToast(int stringId){
        Toast.makeText(_context, stringId, Toast.LENGTH_LONG).show();
    }

    public static boolean isMetzukanServiceRunning() {
        ActivityManager manager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MetzukanService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Write to a text cache file
     * @param path The file name/path
     * @param data The string data to save
     */
    private static void WriteCache(String path, String data) {
        FileOutputStream fOut = null;
        try {
            fOut = _context.openFileOutput(path, _context.MODE_PRIVATE);
            fOut.write(data.getBytes());
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the string data from a text cache file
     * @param path The file path/name
     * @return The file content, or empty if not exists
     */
    private static String ReadCache(String path) {
        String temp = "";

        try {
            FileInputStream fin = _context.openFileInput(path);
            int c;
            while ((c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
        } catch (Exception e) {
            temp = "";
        }

        return temp;
    }
}
