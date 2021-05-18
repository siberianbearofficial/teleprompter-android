package com.example.TeleprompterAndroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText NameET, EmailET, PassET;

    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authHelper = new AuthHelper(this);

        NameET = findViewById(R.id.name_sign_up_et);
        EmailET = findViewById(R.id.email_sign_up_et);
        PassET = findViewById(R.id.password_sign_up_et);
    }

    public void SignUp (View view) {
        String emailString = EmailET.getText().toString();
        String passString = PassET.getText().toString();
        String nameString = NameET.getText().toString();

        if (authHelper.isPasswordCorrect(passString) && authHelper.isEmailCorrect(emailString)) {
            authHelper.registerUser(nameString, emailString, passString);
        }
    }

    public void ToLoginActivity (View view) {
        startActivity(new Intent(getApplicationContext(), StartActivity.class));
    }
}