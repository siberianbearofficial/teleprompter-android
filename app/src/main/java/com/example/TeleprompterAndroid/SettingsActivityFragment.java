package com.example.TeleprompterAndroid;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;
import static com.example.TeleprompterAndroid.Consts.SETTINGS;
import static com.example.TeleprompterAndroid.Consts.colors;

public class SettingsActivityFragment extends Fragment {

    private SeekBar speedSB;
    private TextView textSizeTV;
    private ImageButton changeTextColorSettings;
    private ImageButton changeBackgroundColorSettings;

    private int textSize = 16;
    private int speedPercent = 50;
    private int textColorId = 0;
    private int backgroundColorId = 0;

    private Settings settings;
    private AuthHelper authHelper;
    private boolean isAuthed;

    public SettingsActivityFragment() {}

    public static SettingsActivityFragment newInstance(boolean isAuthed) {
        SettingsActivityFragment fragment = new SettingsActivityFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_AUTHED, isAuthed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAuthed = getArguments().getBoolean(IS_AUTHED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_settings_activity, container, false);

        settings = new Settings();

        if (isAuthed) {
            authHelper = new AuthHelper(getActivity());
        }

        speedSB = layout.findViewById(R.id.speed_seekbar_settings_fragment);
        textSizeTV = layout.findViewById(R.id.text_size_tv_settings_fragment);

        layout.findViewById(R.id.IncreaseTextSizeSettings_fragment).setOnClickListener(v -> {
            IncreaseTextSizeSettings();
        });
        layout.findViewById(R.id.DecreaseTextSizeSettings_fragment).setOnClickListener(v -> {
            DecreaseTextSizeSettings();
        });
        layout.findViewById(R.id.AboutThisApp_fragment).setOnClickListener(v -> {
            AboutThisApp();
        });

        changeTextColorSettings = layout.findViewById(R.id.change_text_color_settings_fragment);
        changeBackgroundColorSettings = layout.findViewById(R.id.change_background_color_settings_fragment);

        changeTextColorSettings.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getActivity());
            dialog.setTitle(getString(R.string.choose_color));
            dialog.setContentView(R.layout.dialog_choose_color);
            setColorsOnClickListeners(dialog, true);
            dialog.show();
        });
        changeBackgroundColorSettings.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getActivity());
            dialog.setTitle(getString(R.string.choose_color));
            dialog.setContentView(R.layout.dialog_choose_color);
            setColorsOnClickListeners(dialog, false);
            dialog.show();
        });

        if (isAuthed) {
            authHelper.getSettingsReference().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int speedPercentGot = NewScrollTEXT.toPercentValue(authHelper.getSpeedFromSnapshot(snapshot));
                    int textSizeGot = authHelper.getTextSizeFromSnapshot(snapshot);
                    int textColorIdGot = authHelper.getTextColorFromSnapshot(snapshot);
                    int backgroundColorIdGot = authHelper.getBackgroundColorFromSnapshot(snapshot);

                    if (speedPercentGot != -1) {
                        speedPercent = speedPercentGot;
                        speedSB.setProgress(speedPercent);
                    }

                    if (textSizeGot != -1) {
                        textSize = textSizeGot;
                        textSizeTV.setText(Integer.toString(textSize));
                    }

                    if (textColorIdGot != -1) {
                        textColorId = textColorIdGot;
                        changeColor(textColorId);
                    }
                    if (backgroundColorIdGot != -1) {
                        backgroundColorId = backgroundColorIdGot;
                        changeBackgroundColor(backgroundColorId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String settingsString = ((NewMainActivity) getActivity()).sharedPreferences.getString(SETTINGS, "-1");
            if (!settingsString.equals("-1")) {
                Gson gson = new Gson();
                settings = gson.fromJson(settingsString, Settings.class);
                textColorId = settings.textColorId;
                backgroundColorId = settings.bgColorId;
                textSize = settings.textSize;
                speedPercent = NewScrollTEXT.toPercentValue(settings.speed);
            }

            textSizeTV.setText(String.valueOf(textSize));
            speedSB.setProgress(speedPercent);

            changeColor(textColorId);
            changeBackgroundColor(backgroundColorId);
        }

        speedSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speedPercent = seekBar.getProgress();
                if (isAuthed) authHelper.saveSpeed(NewScrollTEXT.toSpeedValue(speedPercent));
                else {
                    saveSettingsToPrefs();
                }
            }
        });

        return layout;
    }

    private void saveSettingsToPrefs() {
        SharedPreferences.Editor editor = ((NewMainActivity) getActivity()).sharedPreferences.edit();
        Gson gson = new Gson();
        settings.bgColorId = backgroundColorId;
        settings.textColorId = textColorId;
        settings.speed = NewScrollTEXT.toSpeedValue(speedPercent);
        settings.textSize = textSize;
        String settingsString = gson.toJson(settings);
        editor.putString(SETTINGS, settingsString);
        editor.apply();
    }

    private final int[] buttonIds = {
            R.id.color_1,
            R.id.color_2,
            R.id.color_3,
            R.id.color_4,
            R.id.color_5,
            R.id.color_6,
            R.id.color_7,
            R.id.color_8,
            R.id.color_9,
            R.id.color_10,
            R.id.color_11,
            R.id.color_12,
            R.id.color_13,
            R.id.color_14,
            R.id.color_15,
            R.id.color_16
    };

    private void setColorsOnClickListeners(Dialog dialog, boolean text) {
        Button[] buttons = new Button[16];

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = dialog.findViewById(buttonIds[i]);
            int finalI = i;
            buttons[i].setOnClickListener(v -> {
                if (text) {
                    textColorId = finalI;
                    changeColor(textColorId);
                    if (isAuthed)
                        authHelper.saveTextColor(textColorId);
                    else
                        saveSettingsToPrefs();
                }
                else {
                    backgroundColorId = finalI;
                    changeBackgroundColor(backgroundColorId);
                    if (isAuthed)
                        authHelper.saveBgColor(backgroundColorId);
                    else
                        saveSettingsToPrefs();
                }
                dialog.dismiss();
            });
        }
    }

    private void changeBackgroundColor(int i) {
        changeBackgroundColorSettings.setBackgroundColor(Color.parseColor(colors[i]));
    }

    private void changeColor(int i) {
        changeTextColorSettings.setBackgroundColor(Color.parseColor(colors[i]));
    }

    private void IncreaseTextSizeSettings () {
        textSize++;
        if (isAuthed) authHelper.saveTextSize(textSize);
        else {
            textSizeTV.setText(String.valueOf(textSize));
            saveSettingsToPrefs();
        }
    }

    private void DecreaseTextSizeSettings () {
        textSize--;
        if (isAuthed) authHelper.saveTextSize(textSize);
        else {
            textSizeTV.setText(String.valueOf(textSize));
            saveSettingsToPrefs();
        }
    }

    private void AboutThisApp () {
        ((NewMainActivity) getActivity()).openAboutFragment();
    }
}