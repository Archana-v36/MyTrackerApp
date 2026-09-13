
package com.trackingsystem.mytrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ProfilePageActivity extends AppCompatActivity {

    EditText etName, etVehicle;
    Button btnSave;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        /// foreground services
        LastScreenManager.save(this, ProfilePageActivity.class.getName());

        etName = findViewById(R.id.etName);
        etVehicle = findViewById(R.id.etVehicle);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();

        btnSave.setOnClickListener(v -> saveProfile());
    }


    void saveProfile() {

        String name = etName.getText().toString().trim();
        String vehicle = etVehicle.getText().toString().trim();

        if (name.isEmpty()) {

            etName.setError("Enter Name");
            return;
        }

        if (vehicle.isEmpty()) {

            etVehicle.setError("Enter Vehicle");
            return;
        }


        String uid =
                mAuth.getCurrentUser().getUid();

        String phone =
                mAuth.getCurrentUser().getPhoneNumber();


        UserModel user =
                new UserModel(
                        name,
                        phone,
                        vehicle,
                        "Pending..."
                );


        FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .setValue(user)
                .addOnSuccessListener(unused -> {

                    SharedPreferences prefs =
                            getSharedPreferences(
                                    "MyTrackerApp",
                                    MODE_PRIVATE
                            );

                    prefs.edit()
                            .putString("userId", uid)
                            .putString("role", "Pending...")
                            .apply();


                    startActivity(new Intent(
                            ProfilePageActivity.this,
                            DashboardActivity.class
                    ));

                    finish();
                });
    }
}

//run successfully.....