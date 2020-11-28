package com.metzukan.club;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.metzukan.club.Utils.BASE_UEL;
import static com.metzukan.club.Utils.GetJwtSecrete;
import static com.metzukan.club.Utils.REQUEST_TIMEOUT_MS;
import static com.metzukan.club.Utils.SetJwtSecrete;
import static com.metzukan.club.Utils.ShowToast;

public class MetzukanAPI {

    /**
     * Send ack
     */
    public static void SendAck(long nextAck, String status) {
        String usersToAck =
                    "{ " +
                            "\"nextAck\" : \"" + nextAck + "\"," +
                            "\"status\" : \"" + status + "\" " +
                            "}" ;

        new AckRequestTask().execute(usersToAck);
    }

    /**
     * Delete user
     */
    public static void DeleteUser() {
        new DeleteUserRequestTask().execute();
    }

    /**
     * Create user
     */
    public static void CreateUser(String nickname,
                                  String firstName,
                                  String lastName,
                                  String selfEmail,
                                  String address,
                                  String freeText,
                                  String contactAName,
                                  String contactAEmail,
                                  String contactBName,
                                  String contactBEmail) {
        String contactBObj = "";
        // If there is am second contact, create the JSON obj for it.
        if(!contactBName.equals("") && !contactBEmail.equals("")) {
            contactBObj =
                    ",{ " +
                            "\"name\" : \"" + contactBName + "\"," +
                            "\"mail\" : \"" + contactBEmail + "\" " +
                            "}" ;
        }

        String usersToCreate =
                " {" +
                        "\"userConstInfo\": {" +
                            "\"selfEmail\": \"" + selfEmail + "\"," +
                            "\"nickname\": \"" + nickname + "\"," +
                            "\"status\" : \"OK\"" +
                        "}," +
                        "\"userSignInfo\": { " +
                            "\"firstName\": \"" +firstName+ "\", " +
                            "\"lastName\": \"" +lastName+ "\", " +
                            "\"address\": \"" +address+ "\", " +
                            "\"freeText\": \"" +freeText + "\"," +
                            "\"contacts\": [" +
                                    "{ " +
                                        "\"name\" : \"" +contactAName + "\"," +
                                        "\"mail\" : \"" +contactAEmail + "\" " +
                                    "}" + contactBObj + "]" +
                        "}" +
        "}";

        new CreateUserRequestTask().execute(usersToCreate);
    }

    private static class AckRequestTask extends AsyncTask<String, String, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            URL urlObj;
            Integer code = 0;

            try {
                urlObj = new URL(BASE_UEL + "/ack");
                HttpURLConnection connection = CreateHttpConnection(urlObj, params[0]);
                connection.setConnectTimeout(REQUEST_TIMEOUT_MS);
                code = connection.getResponseCode();
            } catch (Exception e) {
                code = 501;
            }

            return code;
        }

        private HttpURLConnection CreateHttpConnection(URL url, String body) throws Exception {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", GetJwtSecrete());

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            return conn;
        }

        @Override
        protected void onPostExecute(Integer resultData) {
            if(resultData / 100 == 2){
                ShowToast(R.string.metzukan_user_ack_toast);
            } else {
                ShowToast(R.string.metzukan_user_ack_failed_toast);
            }
        }
    }

    private static class DeleteUserRequestTask extends AsyncTask<String, String, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            URL urlObj;
            Integer code = 0;
            try {
                urlObj = new URL(BASE_UEL + "/users");

                HttpURLConnection connection = CreateHttpConnection(urlObj);
                connection.setConnectTimeout(REQUEST_TIMEOUT_MS);
                code = connection.getResponseCode();
            } catch (Exception e) {
                code = 501;
                e.printStackTrace();
            }

            return code;
        }

        private HttpURLConnection CreateHttpConnection(URL url) throws Exception {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", GetJwtSecrete());
            return conn;
        }

        @Override
        protected void onPostExecute(Integer resultData) {
            if(resultData / 100 == 2){
                SetJwtSecrete("");
                ShowToast(R.string.metzukan_user_removed_toast);
            } else {
                ShowToast(R.string.metzukan_user_removed_failed_toast);
            }
        }
    }

    private static class CreateUserRequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            URL urlObj;
            String result = "";
            try {
                urlObj = new URL(BASE_UEL + "/users");

                HttpURLConnection connection = CreateHttpConnection(urlObj, params[0]);
                connection.setConnectTimeout(REQUEST_TIMEOUT_MS);
                Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                // Load the res body
                for (int c; (c = in.read()) >= 0;)
                    result += (char)c;

            } catch (Exception e) {

            }

            return result;
        }

        private HttpURLConnection CreateHttpConnection(URL url, String body) throws Exception {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            return conn;
        }

        @Override
        protected void onPostExecute(String resultData) {
            // If results contains a new JWT, collect it.
            try {
                //if all OK, should be a token ijn the body res
                if(!resultData.equals("")){
                    // extract the token value
                    String token = resultData.split("Bearer ")[1].split("\"")[0];
                    // save the JWT into session
                    SetJwtSecrete("Bearer " + token);
                    ShowToast(R.string.metzukan_user_created_toast);

                    // Restart the ack countdown
                    MetzukanLogic.SubmitOK();
                }

            } catch (Exception e) {
                ShowToast(R.string.metzukan_user_created_failed_toast);
            }
        }
    }
}
