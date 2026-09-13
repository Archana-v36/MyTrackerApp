
package com.trackingsystem.mytrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    Button getStartedBtn;

    // Use SAME Pref system
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_FIRST_TIME = "isFirstTime";
    private static final String KEY_LOGIN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        //  AUTO SKIP LOGIC (VERY IMPORTANT)
        boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);
        boolean isLoggedIn = prefs.getBoolean(KEY_LOGIN, false);

        if (!isFirstTime) {

            if (isLoggedIn) {
                // Already logged in → go to QR
                startActivity(new Intent(this, AdminQrcode.class));
            } else {
                // Not logged in → go to Log-in
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        getStartedBtn = findViewById(R.id.getStartedBtn);

        getStartedBtn.setOnClickListener(v -> {

            // Mark first time completed
            prefs.edit()
                    .putBoolean(KEY_FIRST_TIME, false)
                    .apply();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}





