package com.example.TeleprompterAndroid;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

import static com.example.TeleprompterAndroid.Consts.BLUETOOTH_SOLICITATION;
import static com.example.TeleprompterAndroid.Consts.DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_READ;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_STATE_CHANGE;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_TOAST;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_WRITE;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTED;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTING;
import static com.example.TeleprompterAndroid.Consts.STATE_LISTEN;
import static com.example.TeleprompterAndroid.Consts.STATE_NONE;
import static com.example.TeleprompterAndroid.Consts.STOP_MODE;

public class WriteActivity extends AppCompatActivity {

    private TextView status;
    private Button btnConnect;
    private Button btnLeaveChat;
    private ListView listView;
    private Dialog dialog;
    private EditText inputLayout;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;

    private ArrayAdapter<String> discoveredDevicesAdapter;
    private BluetoothDevice connectingDevice;
    private WriteController writeController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        findViewsByIds();

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

        // Displays dialog with list of devices
        btnConnect.setOnClickListener(view -> showDevicesDialog());

        // will fechat current chat
        btnLeaveChat.setVisibility(View.INVISIBLE);
        btnLeaveChat.setOnClickListener(view -> chatLeave());

        // Configure conversation adapters
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        listView.setAdapter(chatAdapter);
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case STATE_CONNECTED:
                            setStatus("Connected to:" + connectingDevice.getName());
                            btnConnect.setEnabled(false);
                            btnConnect.setVisibility(View.INVISIBLE);
                            btnLeaveChat.setVisibility(View.VISIBLE);
                            break;
                        case STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(false);
                            break;
                        case STATE_LISTEN:
                        case STATE_NONE:
                            setStatus("Not connected");
                            btnConnect.setEnabled(true);
                            btnConnect.setVisibility(View.VISIBLE);
                            btnLeaveChat.setVisibility(View.INVISIBLE);
                            chatMessages.clear();
                            chatAdapter.notifyDataSetChanged();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatMessages.add("Me: " + writeMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chatMessages.add(connectingDevice.getName() + ":  " + readMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void setStatus(String s) {
        status.setText(s);
    }

    public void chatLeave(){
        btnLeaveChat.setVisibility(View.INVISIBLE);
        if (writeController != null)
            writeController.stop();
        btnConnect.setVisibility(View.VISIBLE);

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
        status = findViewById(R.id.status_write);
        btnConnect = findViewById(R.id.btn_connect_write);
        btnLeaveChat = findViewById(R.id.btn_sair_write);
        listView = findViewById(R.id.list_write);
        inputLayout = findViewById(R.id.input_layout_write);
        View btnSend = findViewById(R.id.btn_send_write);
        View btnStop = findViewById(R.id.btn_stop_write);

        btnStop.setOnClickListener(v -> changeMode(STOP_MODE));

        //TODO: Add more buttons with another functions
        btnSend.setOnClickListener(view -> {
            if (inputLayout.getText().toString().equals("")) {
                Toast.makeText(WriteActivity.this, "Type some text", Toast.LENGTH_SHORT).show();
            } else {
                changeScript(inputLayout.getText().toString());
                inputLayout.setText("");
            }
        });
    }

    private void changeScript (String script) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (script.length() > 0) {
            writeController.changeScript(script);
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
    }

    private void changeMirroring (boolean mirroring) {
        if (writeController.getStatus() != STATE_CONNECTED) {
            Toast.makeText(this, "No connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        writeController.changeMirroring(mirroring);
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