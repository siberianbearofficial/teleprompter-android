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

import com.chinalwb.are.AREditText;
import com.chinalwb.are.AREditor;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.FILE_SPEED;
import static com.example.TeleprompterAndroid.Consts.FILE_TEXT_SIZE;

public class EditorActivity extends AppCompatActivity {

    private ImageButton SaveTextButton, PlayButton;
    private AREditor arEditor;
    private EditText titleET;
    private int textSize;
    private int speed;
    private String script;

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

        Intent intent = getIntent();
        textSize = Integer.parseInt(intent.getStringExtra(FILE_TEXT_SIZE));
        speed = Integer.parseInt(intent.getStringExtra(FILE_SPEED));
        script = intent.getStringExtra(FILE_SCRIPT);

        AREditText arEditText = arEditor.getARE();
        arEditText.setText(script);
        arEditText.setTextSize(textSize);

        SaveTextButton.setOnClickListener(v -> {
            script = arEditor.getHtml();
            new Thread(() -> {
                try {
                    Send(titleET.getText().toString(), script);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("TEXT", script).putExtra("TITLE", titleET.getText().toString()));
            }).start();
        });

        PlayButton.setOnClickListener(v -> {
            script = arEditor.getHtml();
            startActivity(new Intent(getApplicationContext(), PlayActivity.class).putExtra("SCRIPT", script).putExtra("TITLE", titleET.getText().toString()).putExtra("TEXTSIZE", Integer.toString(textSize)).putExtra("SPEED", Integer.toString(speed)));
        });
    }

    private void Send (String title, String text) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();

        RequestBody jsonBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                new Gson().toJson(new Article(title, text)));

        Request request = new Request.Builder()
                .url(MainActivity.url + "postText")
                .post(jsonBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        Log.d("RESPONSE", response.body().string());
    }

    static class Article {
        private String title;
        private String text;
        Article (String title, String text) {
            this.text = text;
            this.title = title;
        }
    }
}