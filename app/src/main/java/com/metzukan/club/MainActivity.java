package com.metzukan.club;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import static com.metzukan.club.MetzukanAPI.CreateUser;
import static com.metzukan.club.MetzukanAPI.DeleteUser;
import static com.metzukan.club.MetzukanLogic.GetLastSubmit;
import static com.metzukan.club.Utils.GetAckIntervalMs;
import static com.metzukan.club.Utils.GetJwtSecrete;
import static com.metzukan.club.Utils.LoadMainContext;
import static com.metzukan.club.Utils.PROGRESS_CHECK_INTERVAL_MS;
import static com.metzukan.club.Utils.SetAckIntervalMs;
import static com.metzukan.club.Utils.isMetzukanServiceRunning;


public class MainActivity extends AppCompatActivity {

    /** app instance, user to update the count-down timer components */
    private static AppCompatActivity _app = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _app = this;

        // Trigger init util
        LoadMainContext(this);

        // Detect the mode, and if the user if active, show the right activity
        String userSession = GetJwtSecrete();

        // If there is not session, show the "create user" interface
        if(userSession.equals("")){
            setWindow(R.layout.create_contact);
        } else {
            setWindow(R.layout.activity_main);
        }

        // Start the time that left thread clock, (show count-down to the next ack)
        TimeLeftClock();

        if(!isMetzukanServiceRunning()) {
            // If MetzukanService not started yet, from any reason, start it right now
            Intent startIntent = new Intent(getApplicationContext(), MetzukanService.class);
            startService(startIntent);
        }
    }

    /**
     * Set the correct window (and register to all needed events...)
     * @param window The window (aka activity) id
     */
    private void setWindow(Integer window) {
        // Set the view
        setContentView(window);

        // then register to the needed events
        if (window == R.layout.activity_main) {
            Button editContactBtn = (Button) findViewById(R.id.edit_contact_button);
            Button pressOkContactBtn = (Button) findViewById(R.id.pressOk);
            // On edit button clicked, move to the create contact view
            editContactBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // To edit contact info, need forest to delete the user, since the user info is sign and saved only by the JWT session
                    DeleteUser();
                    // Set the create user window
                    setWindow(R.layout.create_contact);
                }
            });
            pressOkContactBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // On OK pressed, trigger it
                    MetzukanLogic.SubmitOK();
                }
            });
        }

        if (window == R.layout.create_contact) {
            Button editContactBtn = (Button) findViewById(R.id.submit_contact_btn);
            // On done button clicked, move to the main view
            editContactBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    // Allocate all inputs values
                    TextView nicknameInput = (TextView) findViewById(R.id.createUser_nickname);
                    TextView firstNameInput = (TextView) findViewById(R.id.createUser_firstName);
                    TextView lastNameInput = (TextView) findViewById(R.id.createUser_lastName);
                    TextView selfMailInput = (TextView) findViewById(R.id.createUser_selfMail);
                    TextView addressInput = (TextView) findViewById(R.id.createUser_address);
                    TextView freeTextInput = (TextView) findViewById(R.id.createUser_freeText);
                    TextView contactNameAInput = (TextView) findViewById(R.id.createUser_contactNameA);
                    TextView contactMailAInput = (TextView) findViewById(R.id.createUser_contactMailA);
                    TextView contactNameBInput = (TextView) findViewById(R.id.createUser_contactNameB);
                    TextView contactMailBInput = (TextView) findViewById(R.id.createUser_contactMailB);
                    TextView minutesInput = (TextView) findViewById(R.id.createUser_minutes);
                    TextView hoursInput = (TextView) findViewById(R.id.createUser_hours);

                    Integer minutes = Integer.parseInt(minutesInput.getText().toString());
                    Integer hours = Integer.parseInt(hoursInput.getText().toString());

                    // Calc the total ms to akc interval
                    Integer totalMs = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000);

                    // Set the ack interval (*before* creating the user in server), to start the countdown immediately
                    SetAckIntervalMs(totalMs);

                    // Send the user to the server, (without awaiting to response..
                    CreateUser(
                            nicknameInput.getText().toString(),
                            firstNameInput.getText().toString(),
                            lastNameInput.getText().toString(),
                            selfMailInput.getText().toString(),
                            addressInput.getText().toString(),
                            freeTextInput.getText().toString(),
                            contactNameAInput.getText().toString(),
                            contactMailAInput.getText().toString(),
                            contactNameBInput.getText().toString(),
                            contactMailBInput.getText().toString());

                    // Move to the main window
                    setWindow(R.layout.activity_main);
                }
            });
        }
    }

    /**
     * The time left countdown clock thread
     */
    private void TimeLeftClock() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(PROGRESS_CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Try get the timer components (text & progress bar)
                            ProgressBar progressBar = (ProgressBar) _app.findViewById(R.id.progressBar);
                            TextView timeToAlert = (TextView) _app.findViewById(R.id.time_to_alert);

                            // Get the last submit
                            long lastSubmit = GetLastSubmit();

                            // get the ack interval
                            long ackInterval = GetAckIntervalMs();

                            // get the time now
                            long now = System.currentTimeMillis();

                            // calc the ms passed from the last submit
                            long msPassed = now - lastSubmit;

                            // calc the ms left till next ack time
                            long msTotalLeft = ackInterval - msPassed;

                            // if not time left, mark it as zero
                            if(msTotalLeft < 0) {
                                msTotalLeft = 0;
                            }

                            // calc the left seconds
                            long secLeft = (msTotalLeft / 1000) % 60;

                            // calc the left minutes
                            long minutesLeft = (msTotalLeft / 1000 / 60) % 60;

                            // calc the left hours
                            long HoursLeft = ((msTotalLeft / 1000/ 60 / 60));

                            // write the left time as HHMMSS to the screen
                            timeToAlert.setText("H' " + HoursLeft + " " + " M' " + minutesLeft + " S' " + secLeft);

                            // set the "full" progress bar as ack interval ms
                            progressBar.setMax((int) ackInterval);

                            // then, set the progress as ms that left till next ack
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressBar.setProgress((int)msTotalLeft, true);
                            } else {
                                progressBar.setProgress((int)msTotalLeft);
                            }

                        } catch (Exception e) {

                        }
                        TimeLeftClock();
                    }
                });
            }
        }).start();
    }
}

