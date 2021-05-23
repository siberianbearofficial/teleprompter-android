package com.example.TeleprompterAndroid;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import static com.example.TeleprompterAndroid.Consts.BLUETOOTH_SOLICITATION;
import static com.example.TeleprompterAndroid.Consts.CHANGE_ALL;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MIRRORING;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MODE;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SPEED;
import static com.example.TeleprompterAndroid.Consts.CHANGE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_READ;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_TOAST;
import static com.example.TeleprompterAndroid.Consts.STATE_NONE;

public class ReadFragment extends Fragment {

    private NewScrollTEXT scrollTextView;
    private LinearLayout containerView;
    private BluetoothAdapter bluetoothAdapter;

    private ReadController readController;

    public ReadFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_read, container, false);
        containerView = layout.findViewById(R.id.container_read_bluetooth_fragment);
        setupScrollingTextView(Color.BLACK, 16);

        // Checks whether the device supports bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth adapter is not working!", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        // Prompts the user to enable bluetooth
        else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_SOLICITATION);
        }

        return layout;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    try {
                        byte[] readBuf = (byte[]) msg.obj;
                        String readMessage = new String(readBuf, 0, msg.arg1);
                    } catch (Exception ignored) {}
                    break;
                case CHANGE_SCRIPT:
                    String script = (String) msg.obj;
                    Log.e("READ_ACTIVITY", "CHANGE_SCRIPT: " + script);
                    scrollTextView.changeScript(script);
                    break;
                case CHANGE_TEXT_SIZE:
                    String textSizeString = (String) msg.obj;
                    int size = Integer.parseInt(textSizeString);
                    scrollTextView.changeTextSize(size);
                    Log.e("READ_ACTIVITY", "CHANGE_TEXT_SIZE: " + textSizeString);
                    break;
                case CHANGE_MIRRORING:
                    String mirroringString = (String) msg.obj;
                    boolean mirroring = mirroringString.equals("true");
                    Log.d("ReadActivity", "CHANGE_MIRRORING: " + mirroringString);
                    scrollTextView.changeMirroring(mirroring);
                    break;
                case CHANGE_SPEED:
                    String speedString = (String) msg.obj;
                    int speed = Integer.parseInt(speedString);
                    Log.d("ReadActivity", "CHANGE_SPEED: " + speedString);
                    scrollTextView.changeSpeed(speed);
                    break;
                case CHANGE_MODE:
                    int mode = Integer.parseInt((String) msg.obj);
                    scrollTextView.changeMode(mode);
                    Log.e("ReadActivity", "CHANGE_MODE: " + mode);
                    break;
                case CHANGE_ALL:
                    String gotString = (String) msg.obj;
                    String[] parts = gotString.split("_");
                    int textsize2 = Integer.parseInt(parts[0]); int speed2 = Integer.parseInt(parts[1]); String script2 = parts[2]; boolean mirroring2 = parts[3].equals("true"); String textColor = parts[4], bgColor = parts[5];
                    scrollTextView.changeTextSize(textsize2); scrollTextView.changeScript(script2); scrollTextView.changeMirroring(mirroring2); scrollTextView.changeSpeed(speed2); scrollTextView.changeTextColor(textColor); scrollTextView.changeBackGroundColor(bgColor);
                    Log.e("ReadActivity", "CHANGE_ALL: textSize - " + textsize2 + ", speed - " + speed2 + ", script - " + script2 + ", mirroring - " + (mirroring2 ? "true" : "false"));
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    try {
                        BluetoothDevice connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                        Toast.makeText(getContext(), "Connected to " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "Bluetooth activated!", Toast.LENGTH_LONG).show();
            readController = new ReadController(handler);
        } else {
            Toast.makeText(getContext(), "Bluetooth has not been activated, the app will be finished in 3 seconds.", Toast.LENGTH_LONG).show();

            new CountDownTimer(3000, 1000) {
                public void onFinish() {
                    getActivity().finish();
                }

                public void onTick(long millisUntilFinished) {
                }
            }.start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BLUETOOTH_SOLICITATION);
        } else {
            readController = new ReadController(handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (readController != null) {
            if (readController.getStatus() == STATE_NONE) {
                readController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (readController != null)
            readController.stop();
    }

    private void setupScrollingTextView (int color, int textSize) {
        scrollTextView = new NewScrollTEXT(getContext());
        scrollTextView.setText("");
        scrollTextView.setTextColor(color);
        scrollTextView.setTextSize(textSize);
        scrollTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_alternates_light));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        containerView.setPadding(30, 0, 30, 20);
        scrollTextView.setLayoutParams(layoutParams);
        containerView.removeAllViews();
        containerView.addView(scrollTextView);
    }
}