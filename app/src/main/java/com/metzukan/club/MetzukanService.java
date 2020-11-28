package com.metzukan.club;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.metzukan.club.Utils.LoadMainContext;
import static com.metzukan.club.Utils.METZUKAN_CHANNEL_ID;
import static com.metzukan.club.Utils.METZUKAN_NOTIFICATION_BACKGROUND_SERVICE_ID;
import static com.metzukan.club.Utils.ShowToast;

public class MetzukanService extends Service {

    private MetzukanLogic ml;

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Init the util, of not init yet
        LoadMainContext(this);

        // create notification channel, to allow app to show notifications
        createNotificationChannel();

        // Start foreground service, to allow service to run in the background
        startMetzukanForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO create singleton
        ml = new MetzukanLogic( this);
        ShowToast(R.string.metzukan_start_toast);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Run the service in the background, so even if the app is closed, the service will rum
     */
    private void startMetzukanForeground(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startForeground(METZUKAN_NOTIFICATION_BACKGROUND_SERVICE_ID, new Notification());
            return;
        }

        String NOTIFICATION_CHANNEL_ID = METZUKAN_CHANNEL_ID;
        String channelName = getString(R.string.metzukan_background_service_name);
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.metzukan_background_notification_msg))
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(METZUKAN_NOTIFICATION_BACKGROUND_SERVICE_ID, notification);
    }

    /**
     * Create/Open a channel of notification, to allow app to show up notifications
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(METZUKAN_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}