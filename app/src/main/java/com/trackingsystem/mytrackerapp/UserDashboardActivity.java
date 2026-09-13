
package com.trackingsystem.mytrackerapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UserDashboardActivity extends AppCompatActivity {

    ImageButton btnLogout;
    Button btnStopTracking, btnStartTracking;

    String vehicleId;
    String username;
    String userId;

    SharedPreferences prefs;

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_VEHICLE_ID = "vehicleId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        LastScreenManager.save(this, UserDashboardActivity.class.getName());

        btnLogout = findViewById(R.id.btnLogout);
        btnStopTracking = findViewById(R.id.btnStopTracking);
        btnStartTracking = findViewById(R.id.btnStartTracking); // NEW

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        vehicleId = getIntent().getStringExtra("vehicleId");
        username = getIntent().getStringExtra("username");

        if (vehicleId == null) {
            vehicleId = prefs.getString(KEY_VEHICLE_ID, null);
        }

        if (vehicleId == null) {
            Toast.makeText(this, "Vehicle not connected", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        prefs.edit().putString(KEY_VEHICLE_ID, vehicleId).apply();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // SAVE USER NAME
        if (username != null) {
            if (username.isEmpty()) username = "User";

            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("name")
                    .setValue(username);
        }

        //  START TRACKING INITIALLY
        startTrackingService();

        btnStopTracking.setOnClickListener(v -> stopTrackingOnly());

        //  NEW START BUTTON
        btnStartTracking.setOnClickListener(v -> startTrackingAgain());

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // START TRACKING AGAIN (MAIN FEATURE)
    private void startTrackingAgain() {

        if (vehicleId == null || userId == null) return;

        //  UPDATE FIREBASE FIRST
        FirebaseDatabase.getInstance()
                .getReference("connections")
                .child(vehicleId)
                .child("userId")
                .setValue(userId);

        FirebaseDatabase.getInstance()
                .getReference("connections")
                .child(vehicleId)
                .child("status")
                .setValue("connected");

        // START SERVICE WITH vehicleId
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra("vehicleId", vehicleId);

        if (Build.VERSION.SDK_INT >= 26)
            startForegroundService(intent);
        else
            startService(intent);

        Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show();
    }

    private void startTrackingService() {

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101
            );
            return;
        }

        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra("vehicleId", vehicleId); // IMPORTANT

        if (Build.VERSION.SDK_INT >= 26)
            startForegroundService(intent);
        else
            startService(intent);
    }

    private void stopTrackingOnly() {

        Intent stopIntent = new Intent(this, LocationService.class);
        stopIntent.setAction("STOP");

        if (Build.VERSION.SDK_INT >= 26)
            startForegroundService(stopIntent);
        else
            startService(stopIntent);

        // REMOVE FROM FIREBASE (BEST WAY)
        FirebaseDatabase.getInstance()
                .getReference("connections")
                .child(vehicleId)
                .removeValue();

        Toast.makeText(this, "Tracking stopped", Toast.LENGTH_LONG).show();
    }

    private void logoutUser() {

        // STOP SERVICE
        Intent stopIntent = new Intent(this, LocationService.class);
        stopIntent.setAction("STOP");
        startService(stopIntent);

        // REMOVE FROM FIREBASE
        if (vehicleId != null) {
            FirebaseDatabase.getInstance()
                    .getReference("connections")
                    .child(vehicleId)
                    .removeValue();
        }

        // CLEAR PREFS
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit().clear().apply();

        getSharedPreferences("AdminPrefs", MODE_PRIVATE)
                .edit().clear().apply();

        FirebaseAuth.getInstance().signOut();

        LastScreenManager.clear(this);

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Stop tracking and logout?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startTrackingService();
        }
    }
}





