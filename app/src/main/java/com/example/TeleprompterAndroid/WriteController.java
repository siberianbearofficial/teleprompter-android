package com.example.TeleprompterAndroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import static com.example.TeleprompterAndroid.Consts.APP_NAME;
import static com.example.TeleprompterAndroid.Consts.CHANGE_ALL;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MIRRORING;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MODE;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SCRIPT_END;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SCRIPT_MIDDLE;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SCRIPT_START;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SPEED;
import static com.example.TeleprompterAndroid.Consts.CHANGE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_STATE_CHANGE;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_TOAST;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_WRITE;
import static com.example.TeleprompterAndroid.Consts.MY_UUID;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTED;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTING;
import static com.example.TeleprompterAndroid.Consts.STATE_LISTEN;
import static com.example.TeleprompterAndroid.Consts.STATE_NONE;
import static com.example.TeleprompterAndroid.Consts.SYSTEM_REGEX;

public class WriteController {

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread serverThread;
    private ClientThread clientThread;
    private WriteThread writeThread;
    private int status;

    public WriteController(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        status = STATE_NONE;

        this.handler = handler;
    }

    // Контроллирует текст, который показывает состояние соединения (в верхней части экрана)
    private synchronized void setStatus(int status) {
        this.status = status;

        handler.obtainMessage(MESSAGE_STATE_CHANGE, status, -1).sendToTarget();
    }

    // Получает статус текущего подключения
    public synchronized int getStatus() {
        return status;
    }

    // ionicia o serviço
    public synchronized void start() {
        // Cancel threads
        if (clientThread != null) {
            clientThread.cancel();
            clientThread = null;
        }

        if (writeThread != null) {
            writeThread.cancel();
            writeThread = null;
        }

        // Обновляет состояние и запускает поток в состоянии приема
        setStatus(STATE_LISTEN);
        if (serverThread == null) {
            serverThread = new AcceptThread();
            serverThread.start();
        }
    }

    // Начинает соединение с устройством
    public synchronized void connect(BluetoothDevice device) {
        if (status == STATE_CONNECTING) {
            if (clientThread != null) {
                clientThread.cancel();
                clientThread = null;
            }
        }

        if (writeThread != null) {
            writeThread.cancel();
            writeThread = null;
        }

        // Здесь было одно условие аналогичное, только с connectedThread

        // starts a thread to connect with a specific device
        clientThread = new ClientThread(device);
        clientThread.start();
        setStatus(STATE_CONNECTING);
    }

    // manages bluetooth connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (clientThread != null) {
            clientThread.cancel();
            clientThread = null;
        }

        if (writeThread != null) {
            writeThread.cancel();
            writeThread = null;
        }
        if (serverThread != null) {
            serverThread.cancel();
            serverThread = null;
        }

        writeThread = new WriteThread(socket);

        // Sends the name of the connected device to the user interface
        Message msg = handler.obtainMessage(MESSAGE_DEVICE_OBJECT);
        Bundle bundle = new Bundle();
        bundle.putParcelable(DEVICE_OBJECT, device);
        msg.setData(bundle);
        handler.sendMessage(msg);

        setStatus(STATE_CONNECTED);
    }

    // stop / cancel all threads
    public synchronized void stop() {
        if (clientThread != null) {
            clientThread.cancel();
            clientThread = null;
        }

        if (writeThread != null) {
            writeThread.cancel();
            writeThread = null;
        }

        if (serverThread != null) {
            serverThread.cancel();
            serverThread = null;
        }
        setStatus(STATE_NONE);
    }

    public void changeAll (int textSize, int speed, String script, boolean mirroring, String textColor, String bgColor) {
        WriteThread writeThread;
        synchronized (this) {
            if (status != STATE_CONNECTED) return;
            writeThread = this.writeThread;
        }
        String toSend = CHANGE_ALL + SYSTEM_REGEX + textSize + "_" + speed + "_" + script + "_" + (mirroring ? "true" : "false") + "_" + textColor + "_" + bgColor;
        writeThread.send(toSend.getBytes());
    }

    public void changeMirroring (boolean mirroring) {
        WriteThread writeThread;
        synchronized (this) {
            if (status != STATE_CONNECTED) return;
            writeThread = this.writeThread;
        }
        String toSend = CHANGE_MIRRORING + SYSTEM_REGEX + (mirroring ? "true" : "false");
        writeThread.send(toSend.getBytes());
    }
    public void changeSpeed (int speed) {
        WriteThread writeThread;
        synchronized (this) {
            if (status != STATE_CONNECTED) return;
            writeThread = this.writeThread;
        }
        String toSend = CHANGE_SPEED + SYSTEM_REGEX + speed;
        writeThread.send(toSend.getBytes());
    }
    public void changeTextSize (int textSize) {
        WriteThread writeThread;
        synchronized (this) {
            if (status != STATE_CONNECTED) return;
            writeThread = this.writeThread;
        }
        String toSend = CHANGE_TEXT_SIZE + SYSTEM_REGEX + textSize;
        writeThread.send(toSend.getBytes());
    }
    public void changeMode (int mode) {
        WriteThread writeThread;
        synchronized (this) {
            if (status != STATE_CONNECTED) return;
            writeThread = this.writeThread;
        }
        String toSend = CHANGE_MODE + SYSTEM_REGEX + mode;
        writeThread.send(toSend.getBytes());
    }

    public void changeScript (String script) {
        WriteThread writeThread;
        synchronized (this) {
            if (status != STATE_CONNECTED) return;
            writeThread = this.writeThread;
        }
        if (script.length() > 500) {
            ArrayList<String> arrayList = new ArrayList<>();
            String[] parts = splitByNumber(script, 500);
            for (int i = 0; i < parts.length; i++) {
                parts[i] = CHANGE_SCRIPT_MIDDLE + SYSTEM_REGEX + parts[i];
            }
            new Thread(() -> {
                writeThread.send((CHANGE_SCRIPT_START + SYSTEM_REGEX + "ok").getBytes());
                for (String string : parts) {
                    try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
                    writeThread.send(string.getBytes());
                }
                try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
                writeThread.send((CHANGE_SCRIPT_END + SYSTEM_REGEX + "ok").getBytes());
            }).start();
        } else {
            String toSend = CHANGE_SCRIPT + SYSTEM_REGEX + script;
            writeThread.send(toSend.getBytes());
        }
    }

    private static String[] splitByNumber(String s, int size) {
        if(s == null || size <= 0)
            return null;
        int chunks = s.length() / size + ((s.length() % size > 0) ? 1 : 0);
        String[] arr = new String[chunks];
        for(int i = 0, j = 0, l = s.length(); i < l; i += size, j++)
            arr[j] = s.substring(i, Math.min(l, i + size));
        return arr;
    }

    private void connectionFailed() {
        Message msg = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect to device.");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Restart the service for listening mode
        WriteController.this.start();
    }

    private void connectionLost() {
        Message msg = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "The connection to the device was lost.");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Restart the service for listening mode
        WriteController.this.start();
    }

    // runs while listening to connection inputs
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket;
            while (status != STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If any connection is as accepted
                if (socket != null) {
                    synchronized (WriteController.this) {
                        switch (status) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // starts a connection thread
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // If not ready to connect or already connected, terminates the socket
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
        }
        // cancel the socket
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    // try to take the action of connecting to someone
    private class ClientThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ClientThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }

        public void run() {
            setName("ConnectThread");

            // cancel the discovery so as not to slow down
            bluetoothAdapter.cancelDiscovery();

            // makes connection by socket
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                return;
            }

            // Ja watertight connected resets the action thread
            synchronized (WriteController.this) {
                clientThread = null;
            }

            // starts the thread with the connection
            connected(socket, device);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }

    // runs during a connection with a remote device
    private class WriteThread {
        private final BluetoothSocket bluetoothSocket;
        private final OutputStream outputStream;

        private StringBuilder stringBuilder = new StringBuilder("");

        public WriteThread(BluetoothSocket socket) {
            this.bluetoothSocket = socket;
            OutputStream tmpOutputStream = null;

            try {
                tmpOutputStream = socket.getOutputStream();
            } catch (IOException e) {}

            this.outputStream = tmpOutputStream;
        }

        // write to output stream
        public void send (byte[] buffer) {
            try {
                outputStream.write(buffer);
                Log.e("WRITE_CONTROLLER", "Row data before sending: " + new String(buffer));
                handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {}
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
