package com.example.TeleprompterAndroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import static com.example.TeleprompterAndroid.Consts.APP_NAME;
import static com.example.TeleprompterAndroid.Consts.DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_DEVICE_OBJECT;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_STATE_CHANGE;
import static com.example.TeleprompterAndroid.Consts.MESSAGE_TOAST;
import static com.example.TeleprompterAndroid.Consts.MY_UUID;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTED;
import static com.example.TeleprompterAndroid.Consts.STATE_CONNECTING;
import static com.example.TeleprompterAndroid.Consts.STATE_LISTEN;
import static com.example.TeleprompterAndroid.Consts.STATE_NONE;
import static com.example.TeleprompterAndroid.Consts.SYSTEM_REGEX;

public class ReadController {

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread serverThread;
    private ClientThread clientThread;
    private ReadThread readThread;
    private int status;

    public ReadController(Handler handler) {
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

        // Cancel running threads
        if (readThread != null) {
            readThread.cancel();
            readThread = null;
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
        if (readThread != null) {
            readThread.cancel();
            readThread = null;
        }

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
        if (readThread != null) {
            readThread.cancel();
            readThread = null;
        }
        if (serverThread != null) {
            serverThread.cancel();
            serverThread = null;
        }

        // Starts a thread to manage the connection and run the transmission
        readThread = new ReadThread(socket);
        readThread.start();

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

        if (readThread != null) {
            readThread.cancel();
            readThread = null;
        }

        if (serverThread != null) {
            serverThread.cancel();
            serverThread = null;
        }
        setStatus(STATE_NONE);
    }

    private void connectionFailed() {
        Message msg = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect to device.");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Restart the service for listening mode
        ReadController.this.start();
    }

    private void connectionLost() {
        Message msg = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "The connection to the device was lost.");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Restart the service for listening mode
        ReadController.this.start();
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
                    synchronized (ReadController.this) {
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
            synchronized (ReadController.this) {
                clientThread = null;
            }

            // starts the thread with the connection
            connected(socket, device);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    // runs during a connection with a remote device
    private class ReadThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;

        public ReadThread(BluetoothSocket socket) {
            this.bluetoothSocket = socket;
            InputStream tmpInputStream = null;

            try {
                tmpInputStream = socket.getInputStream();
            } catch (IOException e) {}

            this.inputStream = tmpInputStream;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // always keep looking at the entrance
            while (true) {
                try {
                    // read the entry
                    bytes = inputStream.read(buffer);
                    String got = new String(buffer, 0, bytes);
                    String[] parts = got.split(SYSTEM_REGEX);

                    // sends the input to be displayed in the user interface
                    Log.e("READ_CONTROLLER", "Bytes: " + bytes);
                    Log.e("READ_CONTROLLER", "Row data: " + got);

                    handler.obtainMessage(Integer.parseInt(parts[0]), bytes, -1, parts[1]).sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    // If something goes wrong restart the chat
                    ReadController.this.start();
                    break;
                }
            }
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
