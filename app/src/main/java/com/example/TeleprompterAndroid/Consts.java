package com.example.TeleprompterAndroid;

import java.util.UUID;

public class Consts {
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public static final int CHANGE_SPEED = 4;
    public static final int CHANGE_SCRIPT = 5;
    public static final int CHANGE_MIRRORING = 6;
    public static final int CHANGE_TEXT_SIZE = 7;
    public static final int CHANGE_MODE = 8;
    public static final int CHANGE_ALL = 18;

    public static final int MESSAGE_STATE_CHANGE = 9;
    public static final int MESSAGE_READ = 10;
    public static final int MESSAGE_WRITE = 11;
    public static final int MESSAGE_DEVICE_OBJECT = 12;
    public static final int MESSAGE_TOAST = 13;

    public static final int BLUETOOTH_SOLICITATION = 14;

    public static final int PAUSE_MODE = 15;
    public static final int PLAY_MODE = 16;
    public static final int STOP_MODE = 17;

    public static final String DEVICE_OBJECT = "device_name";
    public static final String SYSTEM_REGEX = "_015CodeSYSregex37_";
    public static final String APP_NAME = "BluetoothChat";

    public static final UUID MY_UUID = UUID.fromString("85293a76-ff18-4797-b07c-987201c2285e");

}
