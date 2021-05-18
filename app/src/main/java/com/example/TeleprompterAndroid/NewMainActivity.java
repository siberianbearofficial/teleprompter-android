package com.example.TeleprompterAndroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private int textsize;
    private int speed;
    private Handler handler;
    private FileHelper fileHelper;
    private TextView userDisplayName;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        textsize = 48;
        speed = 30;

        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");

        //database = FirebaseDatabase.getInstance();
        //myRef = database.getReference("message");

        userDisplayName = findViewById(R.id.user_displayName);

        //myRef.setValue(name);

        /*myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                userDisplayName.setText(value);
                Log.d("FirebaseRealtimeDatabase", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FirebaseRealtimeDatabase", "Failed to read value.", error.toException());
            }
        });*/

        //Show all files to open
        int count = 10;
        /*ArrayList<String> fileNames = new ArrayList<>();
        ArrayList<String> fileDates = new ArrayList<>();
        ArrayList<FileFragment> files = new ArrayList<>();*/

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        Random random = new Random();
        for (int i = 0; i < count; i++) {
            FileFragment fileFragment = new FileFragment();
            Bundle args = new Bundle();
            args.putString(FILE_NAME, getString(R.string.test_file_name));
            args.putString(FILE_DATE, getString(R.string.test_date));
            args.putBoolean(FILE_STAR, random.nextBoolean());
            args.putInt(FILE_TEXT_SIZE, textsize);
            args.putInt(FILE_SPEED, speed);
            args.putString(FILE_SCRIPT, "Test Script NEW!");
            fileFragment.setArguments(args);
            fragmentTransaction.add(R.id.files_container, fileFragment);
        }

        fragmentTransaction.commit();

        fileHelper = new FileHelper(this);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                startActivity(fileHelper.prepareIntent(msg, textsize, speed));
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fileHelper.sendFileToHandler(requestCode, resultCode, data, handler);
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

    public void BluetoothWriteMode(View v) {
        //startActivity(new Intent(getApplicationContext(), WriteActivity.class).putExtra("SCRIPT", "Test script").putExtra("TEXTSIZE", Integer.toString(textsize)).putExtra("SPEED", Integer.toString(speed)));
        Toast.makeText(getApplicationContext(), "Not available feature", Toast.LENGTH_SHORT).show();
    }

    public void Create (View view) {startActivity(new Intent(getApplicationContext(), EditorActivityBluetoothDevice.class).putExtra(FILE_SPEED, Integer.toString(speed)).putExtra(FILE_TEXT_SIZE, Integer.toString(textsize)).putExtra(FILE_SCRIPT, "Write something..."));}

    public void Upload (View view) {fileHelper.openFile();}

    public void Broadcast (View view) {startActivity(new Intent(getApplicationContext(), EditorActivity.class).putExtra(FILE_SPEED, Integer.toString(speed)).putExtra(FILE_TEXT_SIZE, Integer.toString(textsize)).putExtra(FILE_SCRIPT, "Write something..."));}

    public void ToSettingsActivity (View view) {startActivity(new Intent(getApplicationContext(), SettingsActivity.class));}
}