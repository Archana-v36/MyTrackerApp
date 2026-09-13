package com.trackingsystem.mytrackerapp;
import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class Vtrack extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase Offline Persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}







