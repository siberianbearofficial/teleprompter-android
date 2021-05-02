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
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.gamadevelopment.scrolltextview.ScrollTextView;

import java.util.Set;

import static com.example.TeleprompterAndroid.Consts.BLUETOOTH_SOLICITATION;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MIRRORING;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MODE;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SPEED;
import static com.example.TeleprompterAndroid.Consts.CHANGE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_READ;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_STATE_CHANGE;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_TOAST;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTED;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTING;
import static com.example.TeleprompterAndroid.Consts.STATE_LISTEN;
import static com.example.TeleprompterAndroid.Consts.STATE_NONE;
import static com.example.TeleprompterAndroid.Consts.PAUSE_MODE;

public class ReadActivity extends AppCompatActivity {

    private TextView status;
    private Button btnConnect;
    private Button btnLeaveChat;
    private NewScrollTEXT scrollTextView;
    private LinearLayout container;
    private Dialog dialog;
    private BluetoothAdapter bluetoothAdapter;

    private ArrayAdapter<String> discoveredDevicesAdapter;
    private BluetoothDevice connectingDevice;
    private ReadController readController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        findViewsByIds();
        setupScrollingTextView(Color.BLACK, 16);

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
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
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
                            break;
                        case STATE_NONE:
                            setStatus("Not connected");
                            btnConnect.setEnabled(true);
                            btnConnect.setVisibility(View.VISIBLE);
                            btnLeaveChat.setVisibility(View.INVISIBLE);
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case CHANGE_SCRIPT:
                    String script = (String) msg.obj;
                    Log.e("READ_ACTIVITY", "CHANGE_SCRIPT: " + script);
                    scrollTextView.changeScript(script);
                    break;
                case CHANGE_TEXT_SIZE:
                    byte[] buffer = (byte[]) msg.obj;
                    String bufferStr = new String(buffer);
                    int size = Integer.parseInt(bufferStr);
                    scrollTextView.changeTextSize(size);
                    break;
                case CHANGE_MIRRORING:
                    String mirroringString = (String) msg.obj;
                    boolean mirroring = mirroringString.equals("true");
                    Log.d("ReadActivity", "CHANGE_MIRRORING: " + mirroringString);
                    scrollTextView.changeMirroring(mirroring);
                    break;
                case CHANGE_SPEED:
                    byte[] buffer3 = (byte[]) msg.obj;
                    String bufferStr3 = new String(buffer3, 0, msg.arg1);
                    int speed = Integer.parseInt(bufferStr3);
                    Log.d("ReadActivity", "CHANGE_SPEED: " + bufferStr3);
                    scrollTextView.changeSpeed(speed);
                    break;
                case CHANGE_MODE:
                    //byte[] buffer4 = (byte[]) msg.obj;
                    //String bufferStr4 = new String(buffer4, 0, msg.arg1);
                    int mode = Integer.parseInt((String) msg.obj);
                    scrollTextView.changeMode(mode);
                    Log.e("ReadActivity", "CHANGE_MODE: " + mode);
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    try {
                        connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                        Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
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
        btnLeaveChat.setVisibility(View.INVISIBLE);
        if (readController != null)
            readController.stop();
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
                    readController = new ReadController(handler);
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
        readController.connect(device);
    }

    private void findViewsByIds() {
        status = findViewById(R.id.status_read);
        //output = findViewById(R.id.text_output_read);
        container = findViewById(R.id.container_read_bluetooth);
        btnConnect = findViewById(R.id.btn_connect_read);
        btnLeaveChat = findViewById(R.id.btn_sair_read);
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
        scrollTextView = new NewScrollTEXT(getApplicationContext());
        scrollTextView.setTextColor(color);
        scrollTextView.setTextSize(textSize);
        scrollTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.test_font));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        container.setPadding(30, 0, 30, 20);
        scrollTextView.setLayoutParams(layoutParams);
        container.removeAllViews();
        container.addView(scrollTextView);
    }
}