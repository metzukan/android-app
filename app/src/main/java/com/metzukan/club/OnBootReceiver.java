package com.metzukan.club;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OnBootReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1)
    {
        // Start the metzukan service in in the background
        Intent intent = new Intent(context, MetzukanService.class);
        context.startService(intent);
    }
}
