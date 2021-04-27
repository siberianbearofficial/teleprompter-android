package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private EditText login, pass;
    private View broadcastButton, bluetoothButton, wifiButton, tabletBroadcastButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        login = findViewById(R.id.email_sign_in_et);
        pass = findViewById(R.id.password_sign_in_et);
        broadcastButton = findViewById(R.id.broadcast_button_sign_in);
        bluetoothButton = findViewById(R.id.bluetooth_button_sign_in);
        wifiButton = findViewById(R.id.wifi_button_sign_in);
        tabletBroadcastButtons = findViewById(R.id.tablet_broadcast_buttons);

        broadcastButton.setOnClickListener(v -> tabletBroadcastButtons.setVisibility((tabletBroadcastButtons.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE));

        bluetoothButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ReadActivity.class)));
        wifiButton.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Функция недоступна", Toast.LENGTH_SHORT).show());

        setTestParams(); //TODO: Remove this string!
    }

    private void setTestParams () {
        login.setText("test@ya.ru");
        pass.setText("12345");
    }

    public void Login (View view) {
        String emailString = login.getText().toString();
        String passString = pass.getText().toString();
        if (emailString.equals("test@ya.ru") && passString.equals("12345")) startActivity(new Intent(getApplicationContext(), NewMainActivity.class));
    }
}