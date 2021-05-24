package com.example.TeleprompterAndroid;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import static android.view.KeyEvent.KEYCODE_BUTTON_B;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;
import static com.example.TeleprompterAndroid.Consts.PAUSE_MODE;
import static com.example.TeleprompterAndroid.Consts.PLAY_MODE;
import static com.example.TeleprompterAndroid.Consts.SETTINGS;
import static com.example.TeleprompterAndroid.Consts.STOP_MODE;
import static com.example.TeleprompterAndroid.Consts.colors;

public class PlayFragment extends Fragment {

    private AuthHelper authHelper;
    private NewScrollTEXT scrollTextView;
    private LinearLayout containerView;

    private boolean mirroring = false;
    private boolean paused = false;
    private int speed = 1;
    private int speedPercent;
    private String script;
    private int textSize = 16;
    private boolean isAuthed = false;
    private boolean canChangeSpeed = true;
    private int textColor = 0, bgColor = 0;

    public PlayFragment() {}

    public static PlayFragment newInstance(String script, boolean isAuthed) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString(FILE_SCRIPT, script);
        args.putBoolean(IS_AUTHED, isAuthed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            script = getArguments().getString(FILE_SCRIPT);
            isAuthed = getArguments().getBoolean(IS_AUTHED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_play, container, false);

        if (isAuthed) {
            authHelper = new AuthHelper(getActivity());

            authHelper.getSettingsReference().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int speedGot = authHelper.getSpeedFromSnapshot(snapshot);
                    int textSizeGot = authHelper.getTextSizeFromSnapshot(snapshot);
                    int textColorGot = authHelper.getTextColorFromSnapshot(snapshot);
                    int bgColorGot = authHelper.getBackgroundColorFromSnapshot(snapshot);

                    if (speedGot != -1) {
                        speed = speedGot;
                        try {
                            changeSpeed(speed);
                        } catch (Exception ignored) {}
                    }

                    if (textSizeGot != -1) {
                        textSize = textSizeGot;
                        try {
                            scrollTextView.changeTextSize(textSize);
                        } catch (Exception ignored) {}
                    }

                    if (textColorGot != -1) {
                        textColor = textColorGot;
                        try {
                            scrollTextView.changeTextColor(colors[textColor]);
                        } catch (Exception ignored) {}
                    }

                    if (bgColorGot != -1) {
                        bgColor = bgColorGot;
                        try {
                            scrollTextView.changeBackGroundColor(colors[bgColor]);
                        } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show(); }
            });
        } else {
            String settingsString = ((NewMainActivity) getActivity()).sharedPreferences.getString(SETTINGS, "-1");
            if (!settingsString.equals("-1")) {
                Gson gson = new Gson();
                Settings settings;
                settings = gson.fromJson(settingsString, Settings.class);
                textColor = settings.textColorId;
                bgColor = settings.bgColorId;
                textSize = settings.textSize;
                speed = settings.speed;
            }
        }

        containerView = layout.findViewById(R.id.container_play_fragment);
        setupScrollingTextView(Color.BLACK, textSize);

        scrollTextView.setText(Html.fromHtml(script, Html.FROM_HTML_MODE_LEGACY));

        scrollTextView.setTextColor(Color.parseColor(colors[textColor]));
        scrollTextView.setBackgroundColor(Color.parseColor(colors[bgColor]));

        return layout;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            if (event.getRepeatCount() == 0) {
                switch (keyCode) {

                    case KEYCODE_VOLUME_UP:
                        Log.d("KeyCode", "Button X");
                        mirroring = !mirroring;
                        scrollTextView.changeMirroring(mirroring);
                        break;

                    case KEYCODE_VOLUME_DOWN:
                        Log.e("KeyCode", "Button A");
                        break;

                    case KEYCODE_BUTTON_B:
                        Log.d("KeyCode", "Button B");
                        scrollTextView.changeMode(STOP_MODE);
                        break;

                    case KEYCODE_ENTER:
                        Log.d("KeyCode", "Button ENTER");
                        paused = !paused;
                        scrollTextView.changeMode(paused ? PAUSE_MODE : PLAY_MODE);
                        break;

                    case KEYCODE_DPAD_UP:
                        Log.d("KeyCode", "Joystick UP");
                        speedPercent += 10;
                        boolean needIncrease = true;
                        if (speedPercent > 100) {
                            speedPercent = 100;
                            needIncrease = false;
                        }
                        speed = NewScrollTEXT.toSpeedValue(speedPercent);
                        if (needIncrease) changeSpeed(speed);
                        break;

                    case KEYCODE_DPAD_DOWN:
                        Log.d("KeyCode", "Joystick DOWN");
                        speedPercent -= 10;
                        boolean needDecrease = true;
                        if (speedPercent < 0) {
                            speedPercent = 0;
                            needDecrease = false;
                        }
                        speed = NewScrollTEXT.toSpeedValue(speedPercent);
                        if (needDecrease) changeSpeed(speed);
                        break;

                    case KEYCODE_DPAD_LEFT:
                        Log.d("KeyCode", "Joystick LEFT");
                        textSize -= 1;
                        scrollTextView.setTextSize(textSize);
                        break;

                    case KEYCODE_DPAD_RIGHT:
                        Log.d("KeyCode", "Joystick RIGHT");
                        textSize += 1;
                        if (textSize < 1) textSize = 1;
                        scrollTextView.setTextSize(textSize);
                        break;

                    default:
                        Log.e("KeyCode", Integer.toString(keyCode));
                        break;
                }
                return true;
            } else {
                int repeatCount = event.getRepeatCount();
                return true;
            }
        }
        return false;
    }

    private void changeSpeed (int speed) {
        if (canChangeSpeed) {
            canChangeSpeed = false;
            new Thread(() -> {
                try {
                    scrollTextView.changeMode(PAUSE_MODE);
                    Thread.sleep(100);
                    scrollTextView.setSpeed(speed);
                    Thread.sleep(100);
                    scrollTextView.changeMode(PLAY_MODE);
                    canChangeSpeed = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void setupScrollingTextView (int color, int textSize) {
        scrollTextView = new NewScrollTEXT(getContext());
        scrollTextView.setText("");
        scrollTextView.setTextColor(color);
        scrollTextView.setTextSize(textSize);
        scrollTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_alternates_light));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        containerView.setPadding(30, 0, 30, 20);
        scrollTextView.setLayoutParams(layoutParams);
        containerView.removeAllViews();
        containerView.addView(scrollTextView);
    }
}