package com.example.TeleprompterAndroid;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String email;
    public String name;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }

}
