package com.example.TeleprompterAndroid;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Settings {
    public int speed;
    public int textSize;

    public Settings() {}

    public Settings(int speed, int textSize) {
        this.speed = speed;
        this.textSize = textSize;
    }
}
