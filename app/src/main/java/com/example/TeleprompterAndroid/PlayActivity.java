package com.example.TeleprompterAndroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamadevelopment.scrolltextview.ScrollTextView;

public class PlayActivity extends AppCompatActivity {

    private ScrollTextView scrollTextView;
    private LinearLayout container;
    private TextView titleTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        container = findViewById(R.id.container_play_activity);
        titleTV = findViewById(R.id.title_play_activity);

        Intent intent = getIntent();
        titleTV.setText(intent.getStringExtra("TITLE"));
        showScrollingText(intent.getStringExtra("TEXT"));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), NewMainActivity.class));
    }

    private void showScrollingText (String text) {
        scrollTextView = new ScrollTextView(getApplicationContext());
        scrollTextView.setTextColor(Color.BLACK);

        scrollTextView.setText(new SpannableString(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)));

        float speed = 1;
        int textSize = 16;
        try {
            speed = 2;
            textSize = 20;
        } catch (Exception e) {e.printStackTrace(); Toast.makeText(getApplicationContext(), R.string.wrong_data, Toast.LENGTH_LONG).show();}

        scrollTextView.setSpeed(speed);
        scrollTextView.setTextSize(textSize);
        scrollTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.test_font));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        container.setPadding(30, 0, 30, 20);
        scrollTextView.setLayoutParams(layoutParams);
        container.removeAllViews();
        container.addView(scrollTextView);
    }
}