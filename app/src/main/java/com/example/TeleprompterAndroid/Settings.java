package com.example.TeleprompterAndroid;


public class Settings {
    public int speed;
    public int textSize;
    public int textColorId;
    public int bgColorId;

    public Settings() {}

    public Settings(int speed, int textSize, int textColorId, int bgColorId) {
        this.speed = speed;
        this.textSize = textSize;
        this.textColorId = textColorId;
        this.bgColorId = bgColorId;
    }
}
