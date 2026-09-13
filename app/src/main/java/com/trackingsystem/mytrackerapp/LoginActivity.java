
package com.trackingsystem.mytrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone, etOtp;
    private Button btnSendOtp, btnVerify;
    private LinearLayout otpLayout;

    private FirebaseAuth mAuth;
    private String verificationId = "";

    SharedPreferences prefs;

    private static final String PREF_NAME = "AdminPrefs";
    private static final String KEY_LOGIN = "isLoggedIn";
    private static final String KEY_ADMIN_ID = "adminId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // USER AUTO LOGIN CHECK
        SharedPreferences userPrefs =
                getSharedPreferences("UserPrefs", MODE_PRIVATE);

        if (userPrefs.getBoolean("isLoggedIn", false)) {

            startActivity(
                    new Intent(
                            LoginActivity.this,
                            UserDashboardActivity.class
                    )
            );

            finish();
            return;
        }


        // ADMIN AUTO LOGIN CHECK
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (prefs.getBoolean(KEY_LOGIN, false)) {

            startActivity(new Intent(
                    LoginActivity.this,
                    ProfilePageActivity.class
            ));

            finish();
            return;
        }


        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerify = findViewById(R.id.btnVerify);
        otpLayout = findViewById(R.id.otpLayout);

        mAuth = FirebaseAuth.getInstance();


        // SEND OTP BUTTON
        btnSendOtp.setOnClickListener(v -> {

            String phone =
                    etPhone.getText().toString().trim();

            if (phone.isEmpty() || phone.length() < 10) {

                Toast.makeText(
                        this,
                        "Enter valid number",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            phone = "+91" + phone;

            otpLayout.setVisibility(View.VISIBLE);

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phone)
                            .setTimeout(
                                    60L,
                                    TimeUnit.SECONDS
                            )
                            .setActivity(this)
                            .setCallbacks(callbacks)
                            .build();

            PhoneAuthProvider.verifyPhoneNumber(options);

        });


        // VERIFY OTP BUTTON
        btnVerify.setOnClickListener(v -> {

            String otp =
                    etOtp.getText().toString().trim();

            if (verificationId.isEmpty()) {

                Toast.makeText(
                        this,
                        "Send OTP First",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (otp.isEmpty()) {

                Toast.makeText(
                        this,
                        "Enter OTP",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(
                            verificationId,
                            otp
                    );

            signInUser(credential);

        });

    }


    private PhoneAuthProvider
            .OnVerificationStateChangedCallbacks callbacks =

            new PhoneAuthProvider
                    .OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(
                        @NonNull PhoneAuthCredential credential) {

                    signInUser(credential);
                }

                @Override
                public void onVerificationFailed(
                        @NonNull FirebaseException e) {

                    Toast.makeText(
                            LoginActivity.this,
                            "Failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }

                @Override
                public void onCodeSent(
                        @NonNull String verId,
                        @NonNull PhoneAuthProvider
                                .ForceResendingToken token) {

                    verificationId = verId;

                    Toast.makeText(
                            LoginActivity.this,
                            "OTP Sent Successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            };


    private void signInUser(
            PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String uid =
                                mAuth.getCurrentUser()
                                        .getUid();

                        prefs.edit()
                                .putBoolean(KEY_LOGIN, true)
                                .putString(KEY_ADMIN_ID, uid)
                                .apply();


                        startActivity(new Intent(
                                LoginActivity.this,
                                ProfilePageActivity.class
                        ));

                        finish();

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Invalid OTP",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

    }
}



