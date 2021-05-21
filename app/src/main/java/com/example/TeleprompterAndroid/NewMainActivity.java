package com.example.TeleprompterAndroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.TeleprompterAndroid.Consts.*;


public class NewMainActivity extends AppCompatActivity {


    private boolean isAuthed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment_view, new MainActivityFragment());
        fragmentTransaction.commit();

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.navigation_bar_container, new NavigationBar());
        fragmentTransaction.commit();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fileHelper.sendFileToHandler(requestCode, resultCode, data, handler);
    }*/

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


}