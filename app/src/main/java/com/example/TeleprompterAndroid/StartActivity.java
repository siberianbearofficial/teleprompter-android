package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private EditText login, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        login = findViewById(R.id.email_login);
        pass = findViewById(R.id.password_login);

        setTestParams(); //TODO: Remove this string!
    }

    private void setTestParams () {
        login.setText("test@ya.ru");
        pass.setText("12345");
    }

    public void Login (View view) {
        String emailString = login.getText().toString();
        String passString = pass.getText().toString();
        if (emailString.equals("test@ya.ru") && passString.equals("12345")) startActivity(new Intent(getApplicationContext(), EditorActivity.class));
    }
}