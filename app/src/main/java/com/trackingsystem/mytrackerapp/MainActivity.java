
package com.trackingsystem.mytrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference ref;

    // SharedPreferences constants
    private static final String PREF_NAME = "MyTrackerApp";
    private static final String KEY_FIRST_TIME = "isFirstTime";
    private static final String KEY_VEHICLE_ID = "vehicleId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // LAST SCREEN RESTORE (VERY IMPORTANT)
        if (LastScreenManager.restoreIfExists(this)) {
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        boolean isFirstTime =
                prefs.getBoolean(KEY_FIRST_TIME, true);

        // FIRST TIME APP OPEN
        if (isFirstTime) {

            startActivity(
                    new Intent(
                            this,
                            WelcomeActivity.class
                    )
            );

            prefs.edit()
                    .putBoolean(KEY_FIRST_TIME, false)
                    .apply();

            finish();
            return;
        }

        // NORMAL FLOW
        new Handler(getMainLooper())
                .postDelayed(this::checkUser, 1500);
    }


    private void checkUser() {

        if (mAuth.getCurrentUser() != null) {

            String uid =
                    mAuth.getCurrentUser().getUid();

            ref = FirebaseDatabase
                    .getInstance()
                    .getReference("Users")
                    .child(uid);

            ref.child("role")
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()
                                && task.getResult().exists()) {

                            String role =
                                    task.getResult()
                                            .getValue(String.class);

                            SharedPreferences prefs =
                                    getSharedPreferences(
                                            PREF_NAME,
                                            MODE_PRIVATE
                                    );

                            String vehicleId =
                                    prefs.getString(
                                            KEY_VEHICLE_ID,
                                            null
                                    );

                            // SESSION BASED REDIRECT
                            if (vehicleId != null
                                    && !vehicleId.isEmpty()) {

                                if ("Admin".equals(role)) {

                                    Intent i =
                                            new Intent(
                                                    this,
                                                    AdminDashboardActivity.class
                                            );

                                    i.putExtra(
                                            "vehicleId",
                                            vehicleId
                                    );

                                    startActivity(i);

                                } else {

                                    Intent i =
                                            new Intent(
                                                    this,
                                                    UserDashboardActivity.class
                                            );

                                    i.putExtra(
                                            "vehicleId",
                                            vehicleId
                                    );

                                    startActivity(i);
                                }

                                finish();
                                return;
                            }


                            // NORMAL FLOW
                            if (role != null) {

                                if (role.equals("Admin")) {

                                    startActivity(
                                            new Intent(
                                                    this,
                                                    AdminQrcode.class
                                            )
                                    );

                                } else if (role.equals("User")) {

                                    startActivity(
                                            new Intent(
                                                    this,
                                                    UserscanQrActivity.class
                                            )
                                    );

                                } else {

                                    startActivity(
                                            new Intent(
                                                    this,
                                                    LoginActivity.class
                                            )
                                    );
                                }

                            } else {

                                startActivity(
                                        new Intent(
                                                this,
                                                ProfilePageActivity.class
                                        )
                                );
                            }

                        } else {

                            startActivity(
                                    new Intent(
                                            this,
                                            LoginActivity.class
                                    )
                            );
                        }

                        finish();
                    });

        } else {

            startActivity(
                    new Intent(
                            this,
                            LoginActivity.class
                    )
            );

            finish();
        }
    }
}