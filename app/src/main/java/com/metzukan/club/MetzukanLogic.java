package com.metzukan.club;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import static com.metzukan.club.MetzukanAPI.SendAck;
import static com.metzukan.club.Utils.GetAckIntervalMs;
import static com.metzukan.club.Utils.GetJwtSecrete;
import static com.metzukan.club.Utils.GetLastSubmitUTCCache;
import static com.metzukan.club.Utils.HideMetzukanNotification;
import static com.metzukan.club.Utils.STATUS_CHECK_INTERVAL_MS;
import static com.metzukan.club.Utils.SetLastSubmitCache;
import static com.metzukan.club.Utils.ShowInfoNotification;
import static com.metzukan.club.Utils.ShowMetzukanNotification;
import static com.metzukan.club.Utils.ShowToast;
import static com.metzukan.club.Utils.TIME_TO_SEND_EMERGENCY_ACK_MS;
import static com.metzukan.club.Utils.TIME_TO_SEND_NOT_RESPONDING_ACK_MS;
import static com.metzukan.club.Utils.startVibrate;
import static com.metzukan.club.Utils.stopVibrate;

/**
 * The logic of ack, aupdate, detect and send the ack messages
 */
public class MetzukanLogic {

    static public Context serviceContext;

    static private Boolean _alert = false;
    static private Boolean _notResponse = false;
    static private Boolean _emergency = false;
    static private long lastSubmit = 0;

    public MetzukanLogic(Context context){
        serviceContext = context;
        // On init, get the last submit from the cache file
        UpdateLastSubmit();

        // Start the metzukan service loop
        new MetzukanTask().execute();
    }



    private static void UpdateLastSubmit(){
        lastSubmit = System.currentTimeMillis();
    }

    public static void SubmitOK(){
        // update last submit
        UpdateLastSubmit();

        // cancel alerts
        _alert = false;
        _notResponse = false;
        _emergency = false;

        // send Ack
        SendAck(lastSubmit + GetAckIntervalMs(), "OK");

        // off vibration
        stopVibrate();

        // Close notification
        HideMetzukanNotification();
    }

    public static long GetLastSubmit(){
        return lastSubmit;
    }

    private static class MetzukanTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
        }

        protected String doInBackground(String... params) {
            try {
                Thread.sleep(STATUS_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String resultData){

            // get the session
            String session = GetJwtSecrete();
            if(session.equals("")){
                // If there is not session, there is nothing to do
                new MetzukanTask().execute();
                return;
            }

            // Get the current and next last ack time
            long now = System.currentTimeMillis();

            long ackInterval = GetAckIntervalMs();

            // calc if it's the time to so something...
            long lastAckTimeArrived = (now - lastSubmit);
            long timeToSendNotResponseAck = lastAckTimeArrived - TIME_TO_SEND_NOT_RESPONDING_ACK_MS;
            long timeToSendEmergencyAck = lastAckTimeArrived - TIME_TO_SEND_EMERGENCY_ACK_MS;

            boolean isLastAckTimeArrived = lastAckTimeArrived >= ackInterval;
            boolean isTimeToSendNotResponseArrived = timeToSendNotResponseAck >= ackInterval;
            boolean isTimeToSendEmergencyAck = timeToSendEmergencyAck >= ackInterval;

            // If the ack time did passed, mark alert to start
            if(isLastAckTimeArrived){
                _alert = true;
                // Start/Continue the alarms...
                ShowMetzukanNotification();
                startVibrate();
            }

            // TODO: detect lower battery
            boolean isLowerBattery = false;

            // If NOT_RESPONDING ack not sent yes, and too match time passed from the original time of last time to ack,
            // or if the battery is lower, send the situation to the server
            if(!_notResponse && (isTimeToSendNotResponseArrived || (isLastAckTimeArrived && isLowerBattery))) {
                SendAck(lastSubmit + GetAckIntervalMs(), "NOT_RESPONDING");
                ShowToast(R.string.metzukan_about_to_send_not_response_ack_toast);
                ShowInfoNotification(R.string.metzukan_about_to_send_not_response_ack_notification);
                _notResponse = true;
            }

            // If EMERGENCY ack not sent yes, and too match time passed from the NOT_RESPONSE ack sent message passed,
            // or if the battery is lower and isLastAckTimeArrived, send the EMERGENCY ack call to the server
            if(!_emergency && (isTimeToSendEmergencyAck || (isTimeToSendNotResponseArrived && isLowerBattery))) {
                SendAck(lastSubmit + GetAckIntervalMs(), "EMERGENCY");
                ShowToast(R.string.metzukan_about_to_send_emergency_ack_toast);
                ShowInfoNotification(R.string.metzukan_about_to_send_emergency_ack_toast_notification);
                _emergency = true;
            }

            // Run the next iteration...
            new MetzukanTask().execute();
        }
    }
}
