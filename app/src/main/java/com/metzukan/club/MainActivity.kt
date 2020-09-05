package com.metzukan.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindow(R.layout.activity_main);

    }

    override fun registerForContextMenu(view: View?) {
        super.registerForContextMenu(view)
    }

    private fun setWindow(window: Int) {
        setContentView(window);

        if( window == R.layout.activity_main) {
            var editContactBtn = findViewById<Button>(R.id.edit_contact_button);
            editContactBtn.setOnClickListener { setWindow(R.layout.create_contact) };
        }

        if( window == R.layout.create_contact) {
            var editContactBtn = findViewById<Button>(R.id.submit_contact_btn);
            editContactBtn.setOnClickListener { setWindow(R.layout.activity_main) };
        }
    }

}