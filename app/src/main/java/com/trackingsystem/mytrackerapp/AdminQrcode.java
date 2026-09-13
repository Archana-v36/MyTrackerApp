
package com.trackingsystem.mytrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

public class AdminQrcode extends AppCompatActivity {

    private Button btnGoToMap, btnGenerate;
    private ImageView qrImage;
    private TextView tvStatus;
    private EditText etVehicleId;

    private DatabaseReference ref;

    private String vehicleId, adminId;

    private static final String PREF_NAME = "AdminPrefs";
    private static final String KEY_VEHICLE_ID = "vehicleId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_qrcode);

        /// foreground services..
        LastScreenManager.save(
                this,
                AdminQrcode.class.getName()
        );

        btnGoToMap = findViewById(R.id.btnGoToMap);
        btnGenerate = findViewById(R.id.btnGenerate);
        qrImage = findViewById(R.id.qrImage);
        tvStatus = findViewById(R.id.tvStatus);
        etVehicleId = findViewById(R.id.etData);


        // FIRST try getting adminId from intent
        adminId = getIntent().getStringExtra("id");


        // BACKUP: if intent null → take from FirebaseAuth
        if (adminId == null) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                adminId = FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid();

            } else {

                Toast.makeText(
                        this,
                        "Session Error: Please login again.",
                        Toast.LENGTH_LONG
                ).show();

                startActivity(
                        new Intent(
                                AdminQrcode.this,
                                LoginActivity.class
                        )
                );

                finish();
                return;
            }
        }


        ref = FirebaseDatabase
                .getInstance()
                .getReference("connections");


        btnGenerate.setOnClickListener(v -> {

            vehicleId =
                    etVehicleId.getText()
                            .toString()
                            .trim();

            if (vehicleId.isEmpty()) {

                Toast.makeText(
                        this,
                        "Enter Vehicle ID",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            generateQR(vehicleId);
        });


        btnGoToMap.setOnClickListener(v -> {

            if (vehicleId != null &&
                    !vehicleId.isEmpty()) {

                Intent intent =
                        new Intent(
                                AdminQrcode.this,
                                AdminDashboardActivity.class
                        );

                intent.putExtra(
                        "vehicleId",
                        vehicleId
                );

                startActivity(intent);

            } else {

                Toast.makeText(
                        this,
                        "Generate QR first",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    private void generateQR(String vehicleId) {

        SharedPreferences prefs =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );

        prefs.edit()
                .putString(
                        KEY_VEHICLE_ID,
                        vehicleId
                )
                .apply();


        try {

            String data =
                    adminId + "_" + vehicleId;


            HashMap<String, Object> map =
                    new HashMap<>();

            map.put("adminId", adminId);
            map.put("active", true);


            ref.child(vehicleId)
                    .setValue(map);

            BarcodeEncoder encoder =
                    new BarcodeEncoder();

            Bitmap bitmap =
                    encoder.encodeBitmap(
                            data,
                            BarcodeFormat.QR_CODE,
                            400,
                            400
                    );


            qrImage.setImageBitmap(bitmap);

            tvStatus.setText("QR Active");


        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "QR Generation Failed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}




