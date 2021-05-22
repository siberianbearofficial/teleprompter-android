package com.example.TeleprompterAndroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
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


    private static boolean isAuthed;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private LinearLayout navigationBar;
    private MainActivityFragment mainActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        navigationBar = findViewById(R.id.navigation_bar_container);

        isAuthed = getIntent().getBooleanExtra(IS_AUTHED, false);

        fragmentManager = getSupportFragmentManager();

        openMainActivityFragment();

        setNavigationBar();
    }

    private void setNavigationBar () {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.navigation_bar_container, new NavigationBar());
        fragmentTransaction.commit();
    }

    public void openEditorActivityFragment(String title, String script) {
        navigationBar.setVisibility(View.GONE);
        fragmentTransaction = fragmentManager.beginTransaction();
        Bundle arguments = new Bundle();
        arguments.putString(FILE_NAME, title);
        arguments.putString(FILE_SCRIPT, script);
        EditorActivityFragment editorActivityFragment = new EditorActivityFragment();
        editorActivityFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, editorActivityFragment);
        fragmentTransaction.commit();
    }

    public void openMainActivityFragment() {
        navigationBar.setVisibility(View.VISIBLE);
        fragmentTransaction = fragmentManager.beginTransaction();
        Bundle arguments = new Bundle();
        arguments.putBoolean(IS_AUTHED, isAuthed);
        mainActivityFragment = new MainActivityFragment();
        mainActivityFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, mainActivityFragment);
        fragmentTransaction.commit();
    }

    public void openSettingsActivityFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment_view, new SettingsActivityFragment());
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mainActivityFragment.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onBackPressed() { Toast.makeText(getApplicationContext(), getString(R.string.use_app_buttons), Toast.LENGTH_SHORT).show(); }
}