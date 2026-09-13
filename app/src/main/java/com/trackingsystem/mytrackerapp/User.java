package com.trackingsystem.mytrackerapp;

public class User {

    public String name;
    public String phone;
    public String role;

    public User() {
        // Required for Firebase
    }

    public User(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.role = ""; // Default empty role
    }

    public User(String name, String phone, String role) {
        this.name = name;
        this.phone = phone;
        this.role = role;
    }
}
