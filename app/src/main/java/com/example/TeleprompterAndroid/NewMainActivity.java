package com.example.TeleprompterAndroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewMainActivity extends AppCompatActivity {

    private int textsize;
    private int speed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        textsize = 48;
        speed = 30;
    }

    private void getAllArticles () throws IOException {
        OkHttpClient httpClient = new OkHttpClient();

        RequestBody jsonBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                new Gson().toJson(new GetAllParams(112)));

        Request request = new Request.Builder()
                .url(MainActivity.url + "postText")
                .post(jsonBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        Log.d("RESPONSE", response.body().string());
    }

    static class GetAllParams {
        private int authCode;
        GetAllParams (int authCode) {
            this.authCode = authCode;
        }
    }

    /*public void Choose (View view) {
        switch (view.getId()) {
            case R.id.button_bluetooth_read_mode:
                startActivity(new Intent(getApplicationContext(), ReadActivity.class));
                break;
            case R.id.button_bluetooth_write_mode:
                startActivity(new Intent(getApplicationContext(), WriteActivity.class));
                break;
            default:
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }*/

    public void BluetoothWriteMode(View v) {
        startActivity(new Intent(getApplicationContext(), WriteActivity.class).putExtra("SCRIPT", "Test script").putExtra("TEXTSIZE", Integer.toString(textsize)).putExtra("SPEED", Integer.toString(speed)));
    }
}