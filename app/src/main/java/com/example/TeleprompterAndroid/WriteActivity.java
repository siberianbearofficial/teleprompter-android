package com.example.TeleprompterAndroid;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.Set;

import static com.example.TeleprompterAndroid.Consts.BLUETOOTH_SOLICITATION;
import static com.example.TeleprompterAndroid.Consts.DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_READ;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_STATE_CHANGE;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_TOAST;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_WRITE;
import static com.example.TeleprompterAndroid.Consts.PLAY_MODE;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTED;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTING;
import static com.example.TeleprompterAndroid.Consts.STATE_LISTEN;
import static com.example.TeleprompterAndroid.Consts.STATE_NONE;
import static com.example.TeleprompterAndroid.Consts.PAUSE_MODE;
import static com.example.TeleprompterAndroid.Consts.STOP_MODE;

public class WriteActivity extends AppCompatActivity {

    private TextView status;
    private TextView textsizeView;
    private View btnConnectView;
    private SeekBar speedView;
    private Dialog dialog;
    private BluetoothAdapter bluetoothAdapter;

    private ArrayAdapter<String> discoveredDevicesAdapter;
    private BluetoothDevice connectingDevice;
    private WriteController writeController;

    private boolean mirroring = false;
    private boolean paused = false;
    private boolean connect = true;
    private int speed = 1;
    private String script = "";
    private int textsize = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_2);

        findViewsByIds();

        Intent intent = getIntent();
        script = intent.getStringExtra("SCRIPT");
        textsize = Integer.parseInt(intent.getStringExtra("TEXTSIZE"));
        speed = Integer.parseInt(intent.getStringExtra("SPEED"));
        textsizeView.setText(Integer.toString(textsize));
        speedView.setProgress(toPercentValue(speed));
        Log.d("WRITE_ACTIVITY: ", "Script: " + script + ", Speed: " + toPercentValue(speed));

        // Checks whether the device supports bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth adapter is not working!", Toast.LENGTH_LONG).show();
            finish();
        }

        // Prompts the user to enable bluetooth
        else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_SOLICITATION);
        }
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
                            btnConnectView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rectangle_9));
                            //changeScript(script);
                            break;
                        case STATE_CONNECTING:
                            setStatus("Подключение...");
                            break;
                        case STATE_LISTEN:
                        case STATE_NONE:
                            setStatus(getString(R.string.connect));
                            btnConnectView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rectangle_8));
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                    changeAll(textsize, speed, script, mirroring);
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
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

        btnConnectView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rectangle_8));
        setStatus(getString(R.string.connect));
    }

    private void showDevicesDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Device");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();

        // Boot adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // Retrieving listviews (getting)
        ListView pairedDeviceList = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView discoveredDeviceList = (ListView) dialog.findViewById(R.id.discoveredDeviceList);

        // Set adapters
        pairedDeviceList.setAdapter(pairedDevicesAdapter);
        discoveredDeviceList.setAdapter(discoveredDevicesAdapter);

        // Records to broadcast when a device is found
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);

        // Record to broadcast when Discovery ends
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);

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
                    Toast.makeText(getApplicationContext(), "Bluetooth activated!", Toast.LENGTH_LONG).show();
                    writeController = new WriteController(this, handler);
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth has not been activated, the app will be finished in 3 seconds.", Toast.LENGTH_LONG).show();

                    new CountDownTimer(3000, 1000) {
                        public void onFinish() {
                            finish();
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

    private void findViewsByIds() {
        status = findViewById(R.id.status_write2);
        btnConnectView = findViewById(R.id.btnConnectView);
        textsizeView = findViewById(R.id.size_textfield);
        speedView = findViewById(R.id.speed_seekbar_control);

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
                speedGot = toSpeedValue(speedGot);
                Log.d("SEEK_BAR", "Speed ready: " + speedGot);
                changeSpeed(speedGot);
            }
        });
    }

    private int toSpeedValue (int speedGot) {
        return (int) (((100 - speedGot) / 100f) * 49 + 1);
    }

    private int toPercentValue (int speedGot) {
        return 100 - ((int) ((speedGot - 1) / 49f * 100));
    }

    public void Stop (View view) {
        changeMode(STOP_MODE);
    }

    public void Pause (View view) {
        paused = !paused;
        changeMode((paused) ? PAUSE_MODE : PLAY_MODE);
    }

    public void IncreaseTextSize (View view) {
        textsize++;
        textsizeView.setText(Integer.toString(textsize));
        changeTextSize(textsize);
    }

    public void ReduceTextSize (View view) {
        textsize--;
        textsizeView.setText(Integer.toString(textsize));
        changeTextSize(textsize);
    }

    public void Mirror (View view) {
        mirroring = !mirroring;
        changeMirroring(mirroring);
    }

    private void Connect () {
        if (connect) showDevicesDialog(); else chatLeave();
        connect = !connect;
    }

    private void changeScript (String script) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (script.length() > 0) {
            writeController.changeScript(script);
        } else {
            Toast.makeText(getApplicationContext(), "Type some text", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeTextSize (int textSize) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (textSize > 0) {
            writeController.changeTextSize(textSize);
        }
    }

    private void changeMode (int mode) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        writeController.changeMode(mode);
    }

    private void changeSpeed (int speed) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        writeController.changeMirroring(mirroring);
    }

    private void changeAll (int textSize, int speed, String script, boolean mirroring) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((speed > 0) && (textSize > 0) && (script.length() > 0)) {
            if (script.length() <= 500) {
                writeController.changeAll(textSize, speed, script, mirroring);
                return;
            }
            writeController.changeAll(textSize, speed, "some", mirroring);
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
            writeController = new WriteController(this, handler);
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