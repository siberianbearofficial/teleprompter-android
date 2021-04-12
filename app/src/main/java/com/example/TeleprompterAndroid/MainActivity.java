package com.example.TeleprompterAndroid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.gamadevelopment.scrolltextview.ScrollTextView;

public class MainActivity extends AppCompatActivity {

    private EditText mainText, speedText, sizeText;
    private Button button, colorButton;
    private ScrollTextView scrollTextView;
    private LinearLayout layout, container;

    private int[] colors = {Color.RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.GRAY, Color.DKGRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.YELLOW};
    private int colorIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        layout = findViewById(R.id.root);
        container = findViewById(R.id.container);
        mainText = findViewById(R.id.text);
        speedText = findViewById(R.id.speed);
        sizeText = findViewById(R.id.text_size);
        colorButton = findViewById(R.id.button_color);

        colorIndex = 0;

        button.setOnClickListener(v -> {
            showScrollingText(true);
        });

        colorButton.setOnClickListener(v -> {
            colorIndex++;
            if (colorIndex == colors.length) colorIndex = 0;
            showScrollingText(false);
        });
    }

    private void showScrollingText (Boolean flag) {
        scrollTextView = new ScrollTextView(getApplicationContext());
        if (flag)
            scrollTextView.setText(R.string.really_long_text);
        else
            scrollTextView.setText(mainText.getText().toString());
        scrollTextView.setSpeed(Float.parseFloat(speedText.getText().toString()));
        scrollTextView.setTextSize(Integer.parseInt(sizeText.getText().toString()));
        scrollTextView.setTextColor(colors[colorIndex]);
        scrollTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        container.removeAllViews();
        container.addView(scrollTextView);
    }
}