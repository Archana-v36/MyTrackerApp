
package com.trackingsystem.mytrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class DashboardActivity extends AppCompatActivity {

    TextView tvWelcome;
    Button btnAdmin, btnUser;

    DatabaseReference database;
    String userId;

    private static final String PREF_NAME = "MyTrackerApp";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /// foreground services
        LastScreenManager.save(this, DashboardActivity.class.getName());

        setContentView(R.layout.activity_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnUser = findViewById(R.id.btnUser);

        database = FirebaseDatabase
                .getInstance()
                .getReference("Users");

        // Check login session
        if (FirebaseAuth.getInstance()
                .getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Session expired. Login again.",
                    Toast.LENGTH_SHORT
            ).show();

            startActivity(
                    new Intent(
                            DashboardActivity.this,
                            LoginActivity.class
                    )
            );

            finish();
            return;
        }

        userId = FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getUid();


        SharedPreferences prefs =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );

        String savedRole =
                prefs.getString(
                        KEY_ROLE,
                        null
                );


        // If role already selected earlier
        if (savedRole != null &&
                !savedRole.equals("Pending...")) {

            redirect(savedRole);
            return;
        }


        // Load user name
        loadUserData();


        // Admin button click
        btnAdmin.setOnClickListener(v ->
                saveRole("Admin"));


        //  User button click
        btnUser.setOnClickListener(v ->
                saveRole("User"));



    }


    // Load name from Firebase
    void loadUserData() {

        database.child(userId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot snapshot) {

                                if (snapshot.exists()) {

                                    String name =
                                            snapshot.child("name")
                                                    .getValue(String.class);

                                    if (name != null) {

                                        tvWelcome.setText(
                                                "Welcome " + name
                                        );

                                    } else {

                                        tvWelcome.setText(
                                                "Welcome User"
                                        );
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                                Toast.makeText(
                                        DashboardActivity.this,
                                        error.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
    }


    // Save role
    void saveRole(String role) {

        database.child(userId)
                .child("role")
                .setValue(role)
                .addOnSuccessListener(unused -> {

                    getSharedPreferences(
                            PREF_NAME,
                            MODE_PRIVATE
                    ).edit()
                            .putString(KEY_ROLE, role)
                            .apply();

                    redirect(role);
                });
    }


    // 🚀 Redirect screen
    void redirect(String role) {

        if (role.equals("Admin")) {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            AdminQrcode.class
                    );

            // PASS ADMIN ID HERE (IMPORTANT FIX)
            intent.putExtra(
                    "id",
                    userId
            );

            startActivity(intent);

        } else {

            startActivity(
                    new Intent(
                            DashboardActivity.this,
                            UserscanQrActivity.class
                    )
            );
        }

        finish();
    }
}