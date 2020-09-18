package com.metzukan.club;

import android.content.Context;
import android.os.AsyncTask;

import java.time.LocalDateTime;

import static com.metzukan.club.Utils.STATUS_CHECK_INTERVAL_MS;
import static com.metzukan.club.Utils.SUBMIT_OK_DURATION_MS;
import static com.metzukan.club.Utils.ShowMetzukanNotification;
import static com.metzukan.club.Utils.startVibrate;
import static com.metzukan.club.Utils.stopVibrate;

public class MetzukanLogic {
    static public Context serviceContext;

    static private Boolean alert = true;
    static private long lastSubmit = 0;

    public MetzukanLogic(Context context){
        serviceContext = context;
        UpdateLastSubmit();
        new MetzukanTask().execute();
    }

    private static void UpdateLastSubmit(){
        lastSubmit = System.currentTimeMillis();
    }
    public static void SubmitOK(){
        alert = false;
        stopVibrate(serviceContext);
        UpdateLastSubmit();
        // Close notification
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

            long now = System.currentTimeMillis();
            if((now - lastSubmit) >= SUBMIT_OK_DURATION_MS){
                alert = true;
            }

            if(alert){
                ShowMetzukanNotification(serviceContext);
                startVibrate(serviceContext);
            }

            // Run the next iteration...
            new MetzukanTask().execute();
        }
    }
}
