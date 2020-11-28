package com.metzukan.club;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * This service used only to catch the notification "OK" button pressed
 */
public class NotificationService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate() {
        new NotificationService();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // On start, just set "submit OK"
        MetzukanLogic.SubmitOK();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}