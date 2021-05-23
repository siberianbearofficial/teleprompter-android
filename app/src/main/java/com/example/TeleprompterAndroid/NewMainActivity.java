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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
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
    private ReadFragment readFragment;
    private WriteFragment writeFragment;
    private PlayFragment playFragment;
    private ProfileFragment profileFragment;

    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        sharedPreferences = getPreferences(0);

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
        shouldUseRC = false;
        if (readFragment != null)
            readFragment.onDestroy();
        if (writeFragment != null)
            writeFragment.onDestroy();
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
        shouldUseRC = false;
        if (readFragment != null)
            readFragment.onDestroy();
        if (writeFragment != null)
            writeFragment.onDestroy();
        navigationBar.setVisibility(View.VISIBLE);
        fragmentTransaction = fragmentManager.beginTransaction();
        Bundle arguments = new Bundle();
        arguments.putBoolean(IS_AUTHED, isAuthed);
        mainActivityFragment = new MainActivityFragment();
        mainActivityFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, mainActivityFragment);
        fragmentTransaction.commit();
    }

    public void openWriteActivityFragment(String script) {
        shouldUseRC = false;
        navigationBar.setVisibility(View.GONE);
        fragmentTransaction = fragmentManager.beginTransaction();
        Bundle arguments = new Bundle();
        arguments.putString(FILE_SCRIPT, script);
        arguments.putBoolean(IS_AUTHED, isAuthed);
        writeFragment = new WriteFragment();
        writeFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, writeFragment);
        fragmentTransaction.commit();
    }

    public void openReadActivityFragment() {
        shouldUseRC = false;
        navigationBar.setVisibility(View.VISIBLE);
        fragmentTransaction = fragmentManager.beginTransaction();
        readFragment = new ReadFragment();
        fragmentTransaction.replace(R.id.main_fragment_view, readFragment);
        fragmentTransaction.commit();
    }

    public void openPlayActivityFragment(String script) {
        shouldUseRC = true;
        if (readFragment != null)
            readFragment.onDestroy();
        if (writeFragment != null)
            writeFragment.onDestroy();
        navigationBar.setVisibility(View.GONE);
        fragmentTransaction = fragmentManager.beginTransaction();
        playFragment = new PlayFragment();
        Bundle arguments = new Bundle();
        arguments.putString(FILE_SCRIPT, script);
        arguments.putBoolean(IS_AUTHED, isAuthed);
        playFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, playFragment);
        fragmentTransaction.commit();
    }

    public void openSettingsActivityFragment() {
        shouldUseRC = false;
        navigationBar.setVisibility(View.VISIBLE);
        if (readFragment != null)
            readFragment.onDestroy();
        if (writeFragment != null)
            writeFragment.onDestroy();
        fragmentTransaction = fragmentManager.beginTransaction();
        SettingsActivityFragment settingsActivityFragment = new SettingsActivityFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(IS_AUTHED, isAuthed);
        settingsActivityFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, settingsActivityFragment);
        fragmentTransaction.commit();
    }

    public void openProfileFragment() {
        shouldUseRC = false;
        if (readFragment != null)
            readFragment.onDestroy();
        if (writeFragment != null)
            writeFragment.onDestroy();
        fragmentTransaction = fragmentManager.beginTransaction();
        profileFragment = new ProfileFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(IS_AUTHED, isAuthed);
        profileFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, profileFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_SOLICITATION) {
            readFragment.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == PICK_JPEG_FILE) {
            profileFragment.onActivityResult(requestCode, resultCode, data);
        } else {
            mainActivityFragment.onActivityResult(requestCode, resultCode, data);
        }
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

    public void openEditorActivityFragmentForServerFile(String fileName) {
        shouldUseRC = false;
        navigationBar.setVisibility(View.GONE);
        fragmentTransaction = fragmentManager.beginTransaction();
        Bundle arguments = new Bundle();
        arguments.putString(FILE_NAME, fileName);
        arguments.putString(FILE_SCRIPT, "");
        arguments.putBoolean(IS_AUTHED, true);
        EditorActivityFragment editorActivityFragment = new EditorActivityFragment();
        editorActivityFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, editorActivityFragment);
        fragmentTransaction.commit();
    }

    private Uri UriForCreatingFile;

    public void setUriForCreatingFile(Uri uri) {
        UriForCreatingFile = uri;
    }

    public Uri getUriForCreatingFile() {
        return UriForCreatingFile;
    }

    public void openEditorActivityFragmentWithFile() {
        shouldUseRC = false;
        navigationBar.setVisibility(View.GONE);
        fragmentTransaction = fragmentManager.beginTransaction();
        Bundle arguments = new Bundle();
        arguments.putString(FILE_NAME, "");
        arguments.putString(FILE_SCRIPT, "");
        arguments.putBoolean(IS_AUTHED, false);
        EditorActivityFragment editorActivityFragment = new EditorActivityFragment();
        editorActivityFragment.setArguments(arguments);
        fragmentTransaction.replace(R.id.main_fragment_view, editorActivityFragment);
        fragmentTransaction.commit();
    }

    public void openAboutFragment() {
        shouldUseRC = false;
        navigationBar.setVisibility(View.GONE);
        fragmentTransaction = fragmentManager.beginTransaction();
        AboutFragment aboutFragment = new AboutFragment();
        fragmentTransaction.replace(R.id.main_fragment_view, aboutFragment);
        fragmentTransaction.commit();
    }

    static class GetAllParams {
        private int authCode;
        GetAllParams (int authCode) {
            this.authCode = authCode;
        }
    }

    @Override
    public void onBackPressed() {
        if (shouldUseRC) {
            openMainActivityFragment();
        } else
        Toast.makeText(getApplicationContext(), getString(R.string.use_app_buttons), Toast.LENGTH_SHORT).show();
    }

    // For Remote control:

    private boolean shouldUseRC = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (shouldUseRC) {
            if (playFragment.onKeyDown(keyCode, event)) return true;
            else return super.onKeyDown(keyCode, event);
        } else return super.onKeyDown(keyCode, event);
    }
}