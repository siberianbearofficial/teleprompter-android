package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chinalwb.are.AREditor;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditorActivity extends AppCompatActivity {

    private ImageButton SaveTextButton, PlayButton;
    private AREditor arEditor;
    private EditText titleET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        SaveTextButton = findViewById(R.id.button_save_text);
        PlayButton = findViewById(R.id.button_play_text);
        titleET = findViewById(R.id.title_editor_et);
        arEditor = findViewById(R.id.areditor);

        arEditor.setExpandMode(AREditor.ExpandMode.FULL);
        arEditor.setHideToolbar(false);
        arEditor.setToolbarAlignment(AREditor.ToolbarAlignment.BOTTOM);

        SaveTextButton.setOnClickListener(v -> {
            String gotText = arEditor.getHtml();
            Thread thread = new Thread(() -> {
//                try {
//                    Send(gotText);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("TEXT", gotText).putExtra("TITLE", titleET.getText().toString()));
            });
            thread.start();
        });

        PlayButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), PlayActivity.class).putExtra("TEXT", arEditor.getHtml()).putExtra("TITLE", titleET.getText().toString())));
    }

    private void Send(String text) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();

        RequestBody jsonBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                "{text: " + text + "}");

        Request request = new Request.Builder()
                .url(MainActivity.url + "postText")
                .post(jsonBody)
                .build();

        Response response = httpClient.newCall(request).execute();
    }
}