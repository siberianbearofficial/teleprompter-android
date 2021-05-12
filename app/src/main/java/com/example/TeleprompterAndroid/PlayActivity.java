package com.example.TeleprompterAndroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_ALL_APPS;
import static android.view.KeyEvent.KEYCODE_APP_SWITCH;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_BUTTON_B;
import static android.view.KeyEvent.KEYCODE_BUTTON_Y;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;
import static android.view.KeyEvent.KEYCODE_Y;
import static com.example.TeleprompterAndroid.Consts.PAUSE_MODE;
import static com.example.TeleprompterAndroid.Consts.PLAY_MODE;
import static com.example.TeleprompterAndroid.Consts.STOP_MODE;


public class PlayActivity extends AppCompatActivity {

    private NewScrollTEXT scrollTextView;
    private LinearLayout container;
    private TextView titleTV;
    private int textSize;
    private int speed;
    private int speedPercent;
    private String script;
    private boolean paused;
    private boolean mirroring;

    private boolean canChangeSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        container = findViewById(R.id.container_play_activity);
        titleTV = findViewById(R.id.title_play_activity);

        Intent intent = getIntent();
        script = intent.getStringExtra("SCRIPT");
        textSize = Integer.parseInt(intent.getStringExtra("TEXTSIZE"));
        speed = Integer.parseInt(intent.getStringExtra("SPEED"));

        speedPercent = NewScrollTEXT.toPercentValue(speed);

        titleTV.setText(intent.getStringExtra("TITLE"));
        setupScrollingTextView(Color.BLACK, textSize);
        scrollTextView.setText(Html.fromHtml(script, Html.FROM_HTML_MODE_LEGACY));
        scrollTextView.setSpeed(speed);
        paused = false;
        mirroring = false;
        canChangeSpeed = true;
    }

    @Override
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
                        speedPercent += 1;
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
                        speedPercent -= 1;
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
        return super.onKeyDown(keyCode, event);
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
        scrollTextView = new NewScrollTEXT(getApplicationContext());
        scrollTextView.setText("");
        scrollTextView.setTextColor(color);
        scrollTextView.setTextSize(textSize);
        scrollTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.test_font));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        container.setPadding(30, 0, 30, 20);
        scrollTextView.setLayoutParams(layoutParams);
        container.removeAllViews();
        container.addView(scrollTextView);
    }
}