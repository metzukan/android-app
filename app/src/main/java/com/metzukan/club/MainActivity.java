package com.metzukan.club;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindow(R.layout.activity_main);

        // If service not started ye, from any reason, start it now
        Intent startIntent = new Intent(getApplicationContext(), MetzukanService.class);
        startService(startIntent);
    }

    private void setWindow(Integer window) {
        setContentView(window);

        if (window == R.layout.activity_main) {
            Button editContactBtn = (Button) findViewById(R.id.edit_contact_button);
            // On edit button clicked, move to the create contact view
            editContactBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setWindow(R.layout.create_contact);
                }
            });
        }

        if (window == R.layout.create_contact) {
            Button editContactBtn = (Button) findViewById(R.id.submit_contact_btn);
            // On done button clicked, move to the main view
            editContactBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setWindow(R.layout.activity_main);
                }
            });
        }
    }
}

