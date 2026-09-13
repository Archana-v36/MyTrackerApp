package com.trackingsystem.mytrackerapp;

public class UserModel {

        public String name, phone, vehicle, role;

        // Empty constructor (REQUIRED for Firebase)
        public UserModel() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public UserModel(String name, String phone, String vehicle, String role) {
            this.name = name;
            this.phone = phone;
            this.vehicle = vehicle;
            this.role = role;

        }
}



