package com.example.TeleprompterAndroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar speedSB;
    private TextView textSizeTV;

    private int textSize;

    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        authHelper = new AuthHelper(this);

        speedSB = findViewById(R.id.speed_seekbar_settings);
        textSizeTV = findViewById(R.id.text_size_tv_settings);

        authHelper.getSettingsReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int speedPercent = NewScrollTEXT.toPercentValue(authHelper.getSpeedFromSnapshot(snapshot));
                int textSizeGot = authHelper.getTextSizeFromSnapshot(snapshot);

                if (speedPercent != -1) {
                    speedSB.setProgress(speedPercent);
                }

                if (textSizeGot != -1) {
                    textSize = textSizeGot;
                    textSizeTV.setText(Integer.toString(textSize));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show(); }
        });

        speedSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { authHelper.saveSpeed(NewScrollTEXT.toSpeedValue(seekBar.getProgress())); }
        });
    }

    public void IncreaseTextSizeSettings (View view) {
        textSize++;
        authHelper.saveTextSize(textSize);
    }

    public void DecreaseTextSizeSettings (View view) {
        textSize--;
        authHelper.saveTextSize(textSize);
    }

    public void AboutThisApp (View view) {
        Toast.makeText(getApplicationContext(), "Just the best app :)", Toast.LENGTH_SHORT).show();
    }
}