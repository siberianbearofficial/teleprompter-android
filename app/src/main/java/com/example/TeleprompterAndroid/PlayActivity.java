package com.example.TeleprompterAndroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.TeleprompterAndroid.Consts.PAUSE_MODE;
import static com.example.TeleprompterAndroid.Consts.PLAY_MODE;


public class PlayActivity extends AppCompatActivity {

    private NewScrollTEXT scrollTextView;
    private LinearLayout container;
    private TextView titleTV;
    private boolean flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        container = findViewById(R.id.container_play_activity);
        titleTV = findViewById(R.id.title_play_activity);

        textSize = 10; flag = true;

        Intent intent = getIntent();
        titleTV.setText(intent.getStringExtra("TITLE"));
        showScrollingText(intent.getStringExtra("TEXT"));
    }

    @Override
    public void onBackPressed() {
        textSize += 10;
        scrollTextView.changeTextSize(textSize);

        scrollTextView.changeMode(flag ? PAUSE_MODE : PLAY_MODE);
        flag = !flag;
    }

    int textSize;

    private void showScrollingText (String text) {
        scrollTextView = new NewScrollTEXT(getApplicationContext());
        scrollTextView.setTextColor(Color.BLACK);

        scrollTextView.setText(new SpannableString(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)));

        float speed = 1;
       // textSize = 16;
        try {
            speed = 2;
            //textSize = 20;
        } catch (Exception e) {e.printStackTrace(); Toast.makeText(getApplicationContext(), R.string.wrong_data, Toast.LENGTH_LONG).show();}

        scrollTextView.setSpeed(speed);
        scrollTextView.setTextSize(textSize);

        scrollTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.test_font));

        scrollTextView.stopNestedScroll();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        container.setPadding(30, 0, 30, 20);
        scrollTextView.setLayoutParams(layoutParams);
        container.removeAllViews();
        container.addView(scrollTextView);
    }
}