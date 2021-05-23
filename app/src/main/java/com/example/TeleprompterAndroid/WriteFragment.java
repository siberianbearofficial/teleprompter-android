package com.example.TeleprompterAndroid;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Set;

import static com.example.TeleprompterAndroid.Consts.BLUETOOTH_SOLICITATION;
import static com.example.TeleprompterAndroid.Consts.DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_STATE_CHANGE;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_TOAST;
import static com.example.TeleprompterAndroid.Consts.PAUSE_MODE;
import static com.example.TeleprompterAndroid.Consts.PLAY_MODE;
import static com.example.TeleprompterAndroid.Consts.SETTINGS;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTED;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTING;
import static com.example.TeleprompterAndroid.Consts.STATE_LISTEN;
import static com.example.TeleprompterAndroid.Consts.STATE_NONE;
import static com.example.TeleprompterAndroid.Consts.STOP_MODE;
import static com.example.TeleprompterAndroid.Consts.colors;

public class WriteFragment extends Fragment {

    private TextView status;
    private TextView textsizeView;
    private ImageButton changeTextColorSettings;
    private ImageButton changeBackgroundColorSettings;
    private ImageButton pauseButton;
    private View btnConnectView;
    private SeekBar speedView;
    private Dialog dialog;
    private BluetoothAdapter bluetoothAdapter;

    private ArrayAdapter<String> discoveredDevicesAdapter;
    private BluetoothDevice connectingDevice;
    private WriteController writeController;

    private AuthHelper authHelper;

    private boolean mirroring = false;
    private boolean paused = false;
    private boolean connect = true;
    private boolean isAuthed;
    private int speed = 1;
    private String script = "";
    private int textSize = 16;
    private int textColor;
    private int bgColor;

    public WriteFragment() {}

    public static WriteFragment newInstance(String script, boolean isAuthed) {
        WriteFragment fragment = new WriteFragment();
        Bundle args = new Bundle();
        args.putString(FILE_SCRIPT, script);
        args.putBoolean(IS_AUTHED, isAuthed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            script = getArguments().getString(FILE_SCRIPT);
            isAuthed = getArguments().getBoolean(IS_AUTHED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_write, container, false);

        findViewsByIds(layout);

        //textsize = Integer.parseInt(intent.getStringExtra("TEXTSIZE"));
        //speed = Integer.parseInt(intent.getStringExtra("SPEED"));

        Log.d("WRITE_ACTIVITY: ", "Script: " + script + ", Speed: " + NewScrollTEXT.toPercentValue(speed));

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

        if (isAuthed) {
            authHelper = new AuthHelper(getActivity());

            authHelper.getSettingsReference().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int speedGot = authHelper.getSpeedFromSnapshot(snapshot);
                    int textSizeGot = authHelper.getTextSizeFromSnapshot(snapshot);
                    int textColorGot = authHelper.getTextColorFromSnapshot(snapshot);
                    int bgColorGor = authHelper.getBackgroundColorFromSnapshot(snapshot);

                    if (speedGot != -1)
                        speed = speedGot;

                    if (textSizeGot != -1)
                        textSize = textSizeGot;

                    if (textColorGot != -1)
                        textColor = textColorGot;

                    if (bgColorGor != -1)
                        bgColor = bgColorGor;

                    textsizeView.setText(Integer.toString(textSize));
                    speedView.setProgress(NewScrollTEXT.toPercentValue(speed));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String settingsString = ((NewMainActivity) getActivity()).sharedPreferences.getString(SETTINGS, "-1");
            if (!settingsString.equals("-1")) {
                Gson gson = new Gson();
                Settings settings;
                settings = gson.fromJson(settingsString, Settings.class);
                textColor = settings.textColorId;
                bgColor = settings.bgColorId;
                textSize = settings.textSize;
                speed = settings.speed;
            }
        }

        textsizeView.setText(Integer.toString(textSize));
        speedView.setProgress(NewScrollTEXT.toPercentValue(speed));

        return layout;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case STATE_CONNECTED:
                            String name = connectingDevice.getName();
                            int maxLength = status.getText().toString().length();
                            if (name.length() > maxLength) {
                                name = name.substring(0, maxLength - 3) + "...";
                            }

                            setStatus(name);
                            btnConnectView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rectangle_9));
                            //changeScript(script);
                            break;
                        case STATE_CONNECTING:
                            setStatus("Подключение...");
                            break;
                        case STATE_LISTEN:
                        case STATE_NONE:
                            try {
                                setStatus(getString(R.string.connect));
                                btnConnectView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rectangle_8));
                            } catch (Exception ignored) {}
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getContext(), "Connected to " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                    changeAll(textSize, speed, script, mirroring, colors[textColor], colors[bgColor]);
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void setStatus(String s) {
        status.setText(s);
    }

    public void chatLeave(){
        if (writeController != null)
            writeController.stop();

        btnConnectView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rectangle_8));
        setStatus(getString(R.string.connect));
    }

    private void showDevicesDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Device");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();

        // Boot adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        // Retrieving listviews (getting)
        ListView pairedDeviceList = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView discoveredDeviceList = (ListView) dialog.findViewById(R.id.discoveredDeviceList);

        // Set adapters
        pairedDeviceList.setAdapter(pairedDevicesAdapter);
        discoveredDeviceList.setAdapter(discoveredDevicesAdapter);

        // Records to broadcast when a device is found
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(broadcastReceiver, filter);

        // Record to broadcast when Discovery ends
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(broadcastReceiver, filter);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // Adds the paired devices adapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add("No paired device!");
        }

        // Setting click action on a list item
        pairedDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            bluetoothAdapter.cancelDiscovery();

            String info = ((TextView) view).getText().toString();
            String macAddress = info.substring(info.length() - 17);

            connectToDevice(macAddress);
            dialog.dismiss();
        });

        discoveredDeviceList.setOnItemClickListener((adapterView, view, i, l) -> {
            bluetoothAdapter.cancelDiscovery();

            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            connectToDevice(address);
            dialog.dismiss();
        });


        dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BLUETOOTH_SOLICITATION:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Bluetooth activated!", Toast.LENGTH_LONG).show();
                    writeController = new WriteController(getContext(), handler);
                } else {
                    Toast.makeText(getContext(), "Bluetooth has not been activated, the app will be finished in 3 seconds.", Toast.LENGTH_LONG).show();

                    new CountDownTimer(3000, 1000) {
                        public void onFinish() {
                            getActivity().finish();
                        }
                        public void onTick(long millisUntilFinished) {
                            // Every 1 second (do nothing)
                        }
                    }.start();
                }
        }
    }

    private void connectToDevice(String macAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        writeController.connect(device);
    }

    private void findViewsByIds(View layout) {
        status = layout.findViewById(R.id.status_write_fragment);
        btnConnectView = layout.findViewById(R.id.btnConnectView_fragment);
        textsizeView = layout.findViewById(R.id.size_textfield_fragment);
        speedView = layout.findViewById(R.id.speed_seekbar_control_fragment);

        pauseButton = layout.findViewById(R.id.pause_button_write_fragment);
        pauseButton.setOnClickListener(v -> {
            Pause();
        });

        layout.findViewById(R.id.stop_button_write_fragment).setOnClickListener(v -> {
            Stop();
        });

        layout.findViewById(R.id.mirroring_button_write_fragment).setOnClickListener(v -> {
            Mirror();
        });

        layout.findViewById(R.id.increase_text_size_button_write_fragment).setOnClickListener(v -> {
            IncreaseTextSize();
        });

        layout.findViewById(R.id.decrease_text_size_button_write_fragment).setOnClickListener(v -> {
            ReduceTextSize();
        });

        layout.findViewById(R.id.go_back_write_fragment).setOnClickListener(v -> {
            ((NewMainActivity) getActivity()).openMainActivityFragment();
        });

        changeTextColorSettings = layout.findViewById(R.id.change_text_color_write_fragment);
        changeBackgroundColorSettings = layout.findViewById(R.id.change_background_color_write_fragment);

        changeTextColorSettings.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getActivity());
            dialog.setTitle(getString(R.string.choose_color));
            dialog.setContentView(R.layout.dialog_choose_color);
            setColorsOnClickListeners(dialog, true);
            dialog.show();
        });
        changeBackgroundColorSettings.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getActivity());
            dialog.setTitle(getString(R.string.choose_color));
            dialog.setContentView(R.layout.dialog_choose_color);
            setColorsOnClickListeners(dialog, false);
            dialog.show();
        });

        btnConnectView.setOnClickListener(v -> Connect());
        speedView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {changeMode(PAUSE_MODE);}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int speedGot = seekBar.getProgress();
                Log.d("SEEK_BAR", "Speed got: " + Integer.toString(speedGot));
                // от 1 до 50, было от 0 до 100; делаем сначала от 0 до 49, потом от 1 до 50
                speedGot = NewScrollTEXT.toSpeedValue(speedGot);
                Log.d("SEEK_BAR", "Speed ready: " + speedGot);
                changeSpeed(speedGot);
            }
        });
    }

    private final int[] buttonIds = {
            R.id.color_1,
            R.id.color_2,
            R.id.color_3,
            R.id.color_4,
            R.id.color_5,
            R.id.color_6,
            R.id.color_7,
            R.id.color_8,
            R.id.color_9,
            R.id.color_10,
            R.id.color_11,
            R.id.color_12,
            R.id.color_13,
            R.id.color_14,
            R.id.color_15,
            R.id.color_16
    };

    private void setColorsOnClickListeners(Dialog dialog, boolean text) {
        Button[] buttons = new Button[16];

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = dialog.findViewById(buttonIds[i]);
            int finalI = i;
            buttons[i].setOnClickListener(v -> {
                if (text) {
                    textColor = finalI;
                    changeColor(textColor);
                }
                else {
                    bgColor = finalI;
                    changeBackgroundColor(bgColor);
                }
                dialog.dismiss();
            });
        }
    }

    private void changeBackgroundColor(int i) {
        changeBackgroundColorSettings.setBackgroundColor(Color.parseColor(colors[i]));
    }

    private void changeColor(int i) {
        changeTextColorSettings.setBackgroundColor(Color.parseColor(colors[i]));
    }

    public void Stop () {
        changeMode(STOP_MODE);
    }

    public void Pause () {
        paused = !paused;
        pauseButton.setImageResource(paused ? R.drawable.play_button_write_fragment : R.drawable.pause_button);
        changeMode((paused) ? PAUSE_MODE : PLAY_MODE);
    }

    public void IncreaseTextSize () {
        textSize++;
        textsizeView.setText(Integer.toString(textSize));
        changeTextSize(textSize);
    }

    public void ReduceTextSize () {
        textSize--;
        textsizeView.setText(Integer.toString(textSize));
        changeTextSize(textSize);
    }

    public void Mirror () {
        mirroring = !mirroring;
        changeMirroring(mirroring);
    }

    private void Connect () {
        if (connect) showDevicesDialog(); else chatLeave();
        connect = !connect;
    }

    private void changeScript (String script) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(getContext(), "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (script.length() > 0) {
            writeController.changeScript(script);
        } else {
            Toast.makeText(getContext(), "Type some text", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeTextSize (int textSize) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(getContext(), "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (textSize > 0) {
            writeController.changeTextSize(textSize);
        }
    }

    private void changeMode (int mode) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(getContext(), "No connection!", Toast.LENGTH_SHORT).show();
            paused = !paused;
            pauseButton.setImageResource(paused ? R.drawable.play_button_write_fragment : R.drawable.pause_button);
            return;
        }

        writeController.changeMode(mode);
    }

    private void changeSpeed (int speed) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(getContext(), "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (speed > 0) {
            writeController.changeSpeed(speed);
        }

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            changeMode(PLAY_MODE);
        }).start();
    }

    private void changeMirroring (boolean mirroring) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(getContext(), "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        writeController.changeMirroring(mirroring);
    }

    private void changeAll (int textSize, int speed, String script, boolean mirroring, String textColor, String bgColor) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(getContext(), "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((speed > 0) && (textSize > 0) && (script.length() > 0)) {
            if (script.length() <= 500) {
                writeController.changeAll(textSize, speed, script, mirroring, textColor, bgColor);
                return;
            }
            writeController.changeAll(textSize, speed, "", mirroring, textColor, bgColor);
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                writeController.changeScript(script);
            }).start();
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add("No device found");
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BLUETOOTH_SOLICITATION);
        } else {
            writeController = new WriteController(getContext(), handler);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (writeController != null) {
            if (writeController.getStatus() == STATE_NONE) {
                writeController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (writeController != null)
            writeController.stop();
    }
}