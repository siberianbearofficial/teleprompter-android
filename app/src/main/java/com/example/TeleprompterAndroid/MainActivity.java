package com.example.TeleprompterAndroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gamadevelopment.scrolltextview.ScrollTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.PICK_HTML_FILE;

public class MainActivity extends AppCompatActivity {

    private EditText mainText, speedText, sizeText;
    private Button button, testButton, fileButton, editorButton;
    private ScrollTextView scrollTextView;
    private LinearLayout container;
    private Handler h, f;

    private int textSize;
    private int speed;

    public static final String url = "https://teleprompter-android-server.siberianbear.repl.co/";

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
        fileButton = findViewById(R.id.buttonFile);
        //editorButton = findViewById(R.id.editorButton);

        speed = 1;
        textSize = 48;

        h = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                showScrollingText(msg.obj.toString());
            }
        };

        f = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = (Bundle) msg.obj;
                String fileName = bundle.getString(FILE_NAME);
                String fileScript = bundle.getString(FILE_SCRIPT);
                if (fileName.equals(getString(R.string.no_file))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_file), Toast.LENGTH_SHORT).show(); return;
                }
                Log.d("MainActivity", "File name: " + fileName);
                startActivity(new Intent(getApplicationContext(), WriteActivity.class).putExtra("SCRIPT", fileScript).putExtra("TEXTSIZE", Integer.toString(textSize)).putExtra("SPEED", Integer.toString(speed)));
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
            Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
            startActivity(intent);
        });

        fileButton.setOnClickListener(v -> {
//            chooseFile();
            openFile();
        });

//        editorButton.setOnClickListener(v -> {
//
//        });
    }

    public Object getHttpResponse() {
        OkHttpClient httpClient = new OkHttpClient();


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

    private void openFile() {
        if (isStoragePermissionGranted()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");

            startActivityForResult(intent, PICK_HTML_FILE);
        }
    }

    /*protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case 1:
            {
                if (resultCode == RESULT_OK)
                {
                    Thread thread = new Thread(() -> {
                        Uri chosenFileUri = data.getData();
                        String filePath = chosenFileUri.getPath();
                        filePath = filePath.split(":")[1];
                        System.out.println(filePath);
                        Message m = new Message();
                        Bundle bundle = new Bundle();
                        try {
                            bundle.putString(FILE_SCRIPT, ReadFile(filePath));
                            String[] parts = filePath.split("/");
                            Log.d("MainActivity", parts[parts.length - 1]);
                            bundle.putString(FILE_NAME, parts[parts.length - 1]);
                        } catch (Exception e) {e.printStackTrace(); bundle.putString(FILE_SCRIPT, getString(R.string.no_file)); bundle.putString(FILE_NAME, getString(R.string.no_file)); Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();}
                        m.obj = bundle;
                        f.sendMessage(m);
                    }); thread.start();
                }
                break;
            }
        }
    }*/
    private Uri uri = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_HTML_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.

            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                Thread thread = new Thread(() -> {
                    String filePath = uri.toString();
                    filePath = filePath.split(":")[1];
                    System.out.println(filePath);
                    Message m = new Message();
                    Bundle bundle = new Bundle();
                    try {
                        bundle.putString(FILE_SCRIPT, getScriptFromUri(uri));
                        String[] parts = filePath.split("/");
                        Log.d("MainActivity", parts[parts.length - 1]);
                        bundle.putString(FILE_NAME, parts[parts.length - 1]);
                    } catch (Exception e) {e.printStackTrace(); bundle.putString(FILE_SCRIPT, getString(R.string.no_file)); bundle.putString(FILE_NAME, getString(R.string.no_file)); Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();}
                    m.obj = bundle;
                    f.sendMessage(m);
                }); thread.start();
            }
        }
    }

    private String getScriptFromUri(Uri uri) {
        String content;
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            content = total.toString();
        } catch (Exception e) {content = e.toString();}
        return content;
    }

//    private String readTextFile(File file) throws IOException {
//        StringBuilder text = new StringBuilder();
//        if (isStoragePermissionGranted()) {
//            FileInputStream inputStream = new FileInputStream(file);
//            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//            String str = "";
//            while ((str = br.readLine()) != null) {
//                text.append(str);
//            }
//        } else {
//            Toast.makeText(getApplicationContext(), "Unfortunately, you can\'t use files from your storage without this permission here", Toast.LENGTH_LONG);
//        }
//        return text.toString();
//    }
//
//    private void chooseFile() {
//        Intent PickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        PickerIntent.setType("*/*");
//        startActivityForResult(PickerIntent, 1);
//    }

    public  boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("PERMISSION","Permission is granted");
            return true;
        } else {

            Log.v("PERMISSION","Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

//    private String ReadFile(String filePathName) {
//        StringBuilder text = new StringBuilder();
//        if (isStoragePermissionGranted()) {
//            try {
//                String storageDirectory = Environment.getExternalStorageDirectory().toString();
//                File file = new File(storageDirectory + "/" + filePathName);
//                FileInputStream inputStream = new FileInputStream(file);
//                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//                String str = "";
//                while ((str = br.readLine()) != null) {
//                    text.append(str);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println(text.toString());
//        }
//        return text.toString();
//    }

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