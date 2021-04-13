package com.example.TeleprompterAndroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gamadevelopment.scrolltextview.ScrollTextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText mainText, speedText, sizeText;
    private Button button, testButton;
    private ScrollTextView scrollTextView;
    private LinearLayout container;
    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        testButton = findViewById(R.id.buttonTest);
        container = findViewById(R.id.container);
        speedText = findViewById(R.id.speed);
        sizeText = findViewById(R.id.text_size);
        mainText = findViewById(R.id.text_main);

        h = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                showScrollingText(msg.obj.toString());
            }
        };

        testButton.setOnClickListener(v -> {
            Thread thread = new Thread(() -> {
                Object response = getHttpResponse();
                System.out.println(response);
                Message m = Message.obtain();
                try {
                    m.obj = response.toString();
                } catch (Exception e) {e.printStackTrace(); m.obj = getString(R.string.no_connection); Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();}
                h.sendMessage(m);
            });
            thread.start();
        });

        button.setOnClickListener(v -> {
            try {
                String string = mainText.getText().toString();
                if (string.equals("")) Toast.makeText(getApplicationContext(), R.string.wrong_data, Toast.LENGTH_LONG).show(); else showScrollingText(string);
            } catch (Exception e) {e.printStackTrace(); Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();}
        });
    }

    public Object getHttpResponse() {
        OkHttpClient httpClient = new OkHttpClient();

        String url = "https://teleprompter-android-server.siberianbear.repl.co/";
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e("TAG", "error in getting response get request okhttp");
        }
        return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case 1:
            {
                if (resultCode == RESULT_OK)
                {
                    Uri chosenFileUri = data.getData();

//                    Cursor cursor = getContentResolver().query( chosenFileUri, null, null, null, null );
//                    cursor.moveToFirst();
//                    String filePath = cursor.getString(0);
//                    cursor.close();
                    String filePath = chosenFileUri.getPath();
                    filePath = filePath.split(":")[1];
                    System.out.println(filePath);
                    ReadFile(filePath);
                }
                break;
            }
        }
    }

    private void chooseFile() {
        Intent PickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        PickerIntent.setType("*/*");
        startActivityForResult(PickerIntent, 1);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("PERMISSION","Permission is granted");
                return true;
            } else {

                Log.v("PERMISSION","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("PERMISSION","Permission is granted");
            return true;
        }
    }

    private void ReadFile(String filePathName) {
        if (isStoragePermissionGranted()) {
            String text = "";
            try {
                // открываем поток для чтения

                String storageDirectory = Environment.getExternalStorageDirectory().toString();
                File file = new File(storageDirectory + "/" + filePathName);
                FileInputStream inputStream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String str = "";
                // читаем содержимое
                while ((str = br.readLine()) != null) {
                    text += str;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(text);

            //Another way

//            String text = "";
//            BufferedReader br = null;
//            try {
//                br = new BufferedReader(new InputStreamReader(
//                        this.openFileInput(storageDirectory + "/" + filePathName)));
//                String gotString;
//                // читаем содержимое
//                while ((gotString = br.readLine()) != null) {
//                    text += gotString;
//                }
//                br.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println(text);

        }
    }

    private void showScrollingText (String text) {
        scrollTextView = new ScrollTextView(getApplicationContext());
        scrollTextView.setTextColor(Color.BLACK);

        scrollTextView.setText(new SpannableString(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)));

        float speed = 1;
        int textSize = 16;
        try {
            speed = Float.parseFloat(speedText.getText().toString());
            textSize = Integer.parseInt(sizeText.getText().toString());
        } catch (Exception e) {e.printStackTrace(); Toast.makeText(getApplicationContext(), R.string.wrong_data, Toast.LENGTH_LONG).show();}

        scrollTextView.setSpeed(speed);
        scrollTextView.setTextSize(textSize);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        container.setPadding(20, 20, 20, 20);
        scrollTextView.setLayoutParams(layoutParams);
        container.removeAllViews();
        container.addView(scrollTextView);
    }
}