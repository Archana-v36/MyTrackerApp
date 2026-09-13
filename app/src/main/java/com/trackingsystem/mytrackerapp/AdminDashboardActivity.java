
package com.trackingsystem.mytrackerapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.trackingsystem.mytrackerapp.databinding.ActivityAdminDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.*;

public class AdminDashboardActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<String, Marker> userMarkers = new HashMap<>();
    private DatabaseReference database;
    private ActivityAdminDashboardBinding binding;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "AdminPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LastScreenManager.save(this, AdminDashboardActivity.class.getName());

        binding.btnAddUser.setOnClickListener(v ->
                startActivity(new Intent(this, AdminQrcode.class)));

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        database = FirebaseDatabase.getInstance().getReference("connections");

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        LatLng defaultLoc = new LatLng(19.0760, 72.8777);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10));

        listenForAllUsersLocation();
    }
    private void listenForAllUsersLocation() {

        database.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                HashSet<String> activeVehicles = new HashSet<>();

                for (DataSnapshot vehicleSnapshot : snapshot.getChildren()) {

                    String vehicleId = vehicleSnapshot.getKey();
                    if (vehicleId == null) continue;

                    String status = vehicleSnapshot.child("status")
                            .getValue(String.class);

                    String userId = vehicleSnapshot.child("userId")
                            .getValue(String.class);

                    Double lat = vehicleSnapshot.child("location")
                            .child("lat").getValue(Double.class);

                    Double lng = vehicleSnapshot.child("location")
                            .child("lng").getValue(Double.class);

                    // MAIN FIX: REMOVE OFFLINE USER COMPLETELY
                    if (!"connected".equals(status)) {

                        Marker marker = userMarkers.get(vehicleId);

                        if (marker != null) {

                            //  CLOSE INFO WINDOW FIRST
                            if (marker.isInfoWindowShown()) {
                                marker.hideInfoWindow();
                            }

                            // REMOVE MARKER
                            marker.remove();
                        }

                        userMarkers.remove(vehicleId);
                        continue;
                    }

                    // ACTIVE USERS

                    if (lat != null && lng != null && userId != null) {

                        activeVehicles.add(vehicleId);

                        fetchUserDetailsAndUpdateMarker(
                                vehicleId, userId, lat, lng, status
                        );
                    }
                }

                removeOfflineMarkers(activeVehicles);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "Database Error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    private void removeOfflineMarkers(HashSet<String> activeVehicles) {

        for (String vehicleId : new HashSet<>(userMarkers.keySet())) {

            if (!activeVehicles.contains(vehicleId)) {

                Marker marker = userMarkers.get(vehicleId);

                if (marker != null) {

                    // CLOSE INFO WINDOW FIRST
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                    }

                    marker.remove();
                }

                userMarkers.remove(vehicleId);
            }
        }
    }

    private void fetchUserDetailsAndUpdateMarker(
            String vehicleId,
            String userId,
            double lat,
            double lng,
            String status) {

        //  EXTRA SAFETY CHECK
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        DatabaseReference userRef =
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //  IMPORTANT: STOP if user not exists
                if (!snapshot.exists()) {
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String vehicle = snapshot.child("vehicle").getValue(String.class);

                // ❗ STRICT fallback (NO vehicleId fallback now)
                if (name == null || name.trim().isEmpty()) {
                    name = "Unknown";
                }

                if (vehicle == null || vehicle.trim().isEmpty()) {
                    vehicle = "N/A";
                }

                updateMarker(vehicleId, name, vehicle, lat, lng, status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "User fetch error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }




    private void updateMarker(
            String vehicleId,
            String name,
            String vehicle,
            double lat,
            double lng,
            String status) {

        LatLng location = new LatLng(lat, lng);

        String snippetData =
                vehicle + "|" + status + "|" + lat + "|" + lng;

        Marker marker = userMarkers.get(vehicleId);

        if (marker == null) {

            marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(location)
                            .title(name)
                            .snippet(snippetData)
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_car))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );

            userMarkers.put(vehicleId, marker);

        } else {

            marker.setPosition(location);
            marker.setTitle(name);
            marker.setSnippet(snippetData);
        }

        if (marker != null) {
            marker.setPosition(location);
            marker.setTitle(name);
            marker.setSnippet(snippetData);
            marker.hideInfoWindow();
            marker.showInfoWindow();

        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
    }

    //  INFO WINDOW
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            @SuppressLint("InflateParams") View view =
                    LayoutInflater.from(AdminDashboardActivity.this)
                            .inflate(R.layout.custom_info_window, null);

            ImageView carIcon = view.findViewById(R.id.carIcon);
            TextView tvName = view.findViewById(R.id.tvName);
            TextView tvVehicle = view.findViewById(R.id.tvVehicle);
            TextView tvStatus = view.findViewById(R.id.tvStatus);
            TextView tvTime = view.findViewById(R.id.tvTime);
            TextView tvLatLng = view.findViewById(R.id.tvLatLng);

            String name = marker.getTitle();

            String[] data = marker.getSnippet() != null
                    ? marker.getSnippet().split("\\|")
                    : new String[]{"N/A", "offline", "0", "0"};

            String vehicle = data[0];
            String status = data[1];
            String lat = data[2];
            String lng = data[3];

            String time = new SimpleDateFormat(
                    "hh:mm:ss a",
                    Locale.getDefault()
            ).format(new Date());

            tvName.setText("User Name : " + name);
            tvVehicle.setText("VehicleNo : " + vehicle);
            tvStatus.setText("Status : " + status);
            tvTime.setText("Time : " + time);
            tvLatLng.setText("Lat : " + lat + "\nLng : " + lng);

            carIcon.setImageResource(R.drawable.location_info_window);

            return view;
        }
    }

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes",
                        (dialog, which) -> logoutAdmin())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutAdmin() {

        sharedPreferences.edit().clear().apply();

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(
                AdminDashboardActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        LastScreenManager.clear(this);

        startActivity(intent);
        finish();
    }
}
