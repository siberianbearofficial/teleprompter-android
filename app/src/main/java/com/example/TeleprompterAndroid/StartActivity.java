package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StartActivity extends AppCompatActivity {

    private EditText login, pass;
    private View broadcastButton, bluetoothButton, wifiButton, tabletBroadcastButtons;
    private AuthHelper authHelper;

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

        authHelper = new AuthHelper(this);

        broadcastButton.setOnClickListener(v -> tabletBroadcastButtons.setVisibility((tabletBroadcastButtons.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE));

        bluetoothButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ReadActivity.class)));
        wifiButton.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Функция недоступна", Toast.LENGTH_SHORT).show());

        setTestParams(); //TODO: Remove this string!
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(getApplicationContext(), NewMainActivity.class));
        }*/
    }


    private void setTestParams () {
        login.setText("test@ya.ru");
        pass.setText("123456");
    }

    public void Login (View view) {
        String emailString = login.getText().toString();
        String passString = pass.getText().toString();

        authHelper.loginUser(emailString, passString);
    }

    public void ToRegistrationActivity (View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    public void Skip (View view) {
        startActivity(new Intent(getApplicationContext(), NewMainActivity.class).putExtra("IS_AUTHED", false));
    }
}