
package com.trackingsystem.mytrackerapp;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    private String vehicleId;
    private String userId;

    private boolean isStopped = false;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //  ALWAYS GET vehicleId FIRST (VERY IMPORTANT)
        SharedPreferences prefs =
                getSharedPreferences("UserPrefs", MODE_PRIVATE);

        if (vehicleId == null) {
            vehicleId = prefs.getString("vehicleId", null);
        }

        // STOP ACTION
        if (intent != null && "STOP".equals(intent.getAction())) {

            isStopped = true;

            if (locationClient != null && locationCallback != null) {
                locationClient.removeLocationUpdates(locationCallback);
            }

            // DELETE FROM FIREBASE (FINAL FIX)
            if (vehicleId != null) {
                FirebaseDatabase.getInstance()
                        .getReference("connections")
                        .child(vehicleId)
                        .removeValue();
            }

            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // GET vehicleId FROM INTENT (backup)
        if (intent != null && intent.getStringExtra("vehicleId") != null) {
            vehicleId = intent.getStringExtra("vehicleId");
        }

        // USER CHECK
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (vehicleId == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // START FOREGROUND
        startForegroundTracking();

        // START LOCATION
        requestLocationUpdates();

        return START_STICKY;
    }

    private void startForegroundTracking() {

        String channelId = "tracking_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "Tracking Service",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Live Tracking Active")
                        .setContentText("Sharing location...")
                        .setSmallIcon(R.drawable.car_symbol_icon)
                        .build();

        startForeground(1, notification);
    }

    private void requestLocationUpdates() {

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        5000
                ).build();

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult result) {

                // MOST IMPORTANT LINE
                if (isStopped) return;

                Location location = result.getLastLocation();

                if (location != null && vehicleId != null && userId != null) {

                    FirebaseDatabase.getInstance()
                            .getReference("connections")
                            .child(vehicleId)
                            .child("location")
                            .child("lat")
                            .setValue(location.getLatitude());

                    FirebaseDatabase.getInstance()
                            .getReference("connections")
                            .child(vehicleId)
                            .child("location")
                            .child("lng")
                            .setValue(location.getLongitude());

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
                }
            }
        };

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                null
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}