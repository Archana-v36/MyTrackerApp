
package com.trackingsystem.mytrackerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class UserscanQrActivity extends AppCompatActivity {

    Button btnScan;
    TextView tvResult, tvDetails;

    DatabaseReference database;
    String userId;

    private SharedPreferences prefs;

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_VEHICLE_ID = "vehicleId";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userscan_qr);


        /// foreground services...
        LastScreenManager.save(
                this,
                UserscanQrActivity.class.getName()
        );


        btnScan = findViewById(R.id.btnScan);
        tvResult = findViewById(R.id.tvResult);
        tvDetails = findViewById(R.id.tvDetails);


        // STEP 1: Try getting userId from Dashboard intent
        userId = getIntent().getStringExtra("id");


        // STEP 2: Backup option → FirebaseAuth
        if (userId == null) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                userId = FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid();

            } else {

                Toast.makeText(
                        this,
                        "Session expired. Login again.",
                        Toast.LENGTH_LONG
                ).show();

                startActivity(
                        new Intent(
                                UserscanQrActivity.this,
                                LoginActivity.class
                        )
                );

                finish();
                return;
            }
        }


        prefs = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );


        database = FirebaseDatabase
                .getInstance()
                .getReference("connections");


        btnScan.setOnClickListener(v -> scanCode());
    }



    // OPEN QR SCANNER
    private void scanCode() {

        ScanOptions options = new ScanOptions();

        options.setPrompt("Scan Vehicle QR");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);

        barcodeLauncher.launch(options);
    }



    // HANDLE SCAN RESULT
    @SuppressLint("SetTextI18n")
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(
                    new ScanContract(),
                    result -> {

                        if (result.getContents() != null) {

                            String scannedData =
                                    result.getContents();


                            // QR FORMAT: adminId_vehicleId
                            if (!scannedData.contains("_")) {

                                Toast.makeText(
                                        this,
                                        "Invalid QR Code!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            String[] parts =
                                    scannedData.split("_");


                            String vehicleId =
                                    parts[1];


                            tvResult.setText(
                                    "Vehicle ID: " + vehicleId
                            );


                            connectUserToVehicle(vehicleId);

                        }

                        else {

                            Toast.makeText(
                                    this,
                                    "Scan Cancelled",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });



    // CONNECT USER WITH VEHICLE
    private void connectUserToVehicle(String vehicleId) {

        database.child(vehicleId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {

                                    Boolean active =
                                            snapshot.child("active")
                                                    .getValue(Boolean.class);


                                    if (active != null && active) {

                                        // SAVE USER CONNECTION
                                        database.child(vehicleId)
                                                .child("userId")
                                                .setValue(userId);

                                        database.child(vehicleId)
                                                .child("status")
                                                .setValue("connected");


                                        // SAVE SESSION LOCALLY
                                        prefs.edit()
                                                .putString(
                                                        KEY_VEHICLE_ID,
                                                        vehicleId
                                                )
                                                .putString(
                                                        KEY_USER_ID,
                                                        userId
                                                )
                                                .apply();


                                        tvDetails.setText(
                                                "Connected Successfully!"
                                        );


                                        Toast.makeText(
                                                UserscanQrActivity.this,
                                                "Connected to Admin",
                                                Toast.LENGTH_SHORT
                                        ).show();


                                        // OPEN USER DASHBOARD
                                        Intent intent =
                                                new Intent(
                                                        UserscanQrActivity.this,
                                                        UserDashboardActivity.class
                                                );

                                        intent.putExtra(
                                                "vehicleId",
                                                vehicleId
                                        );

                                        intent.putExtra(
                                                "userId",
                                                userId
                                        );

                                        startActivity(intent);

                                        finish();

                                    }

                                    else {

                                        tvDetails.setText(
                                                "QR not active!"
                                        );

                                        Toast.makeText(
                                                UserscanQrActivity.this,
                                                "This QR is not active",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }

                                else {

                                    tvDetails.setText(
                                            "Invalid QR Code!"
                                    );

                                    Toast.makeText(
                                            UserscanQrActivity.this,
                                            "Invalid QR Code!",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                Toast.makeText(
                                        UserscanQrActivity.this,
                                        "Error: " + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
    }
}



