package com.example.TeleprompterAndroid;

import android.content.Context;
import android.graphics.Color;
import android.icu.util.Measure;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.Toast;

import androidx.annotation.NonNull;

import static com.example.TeleprompterAndroid.Consts.CHANGE_BACKGROUND_COLOR;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MIRRORING;
import static com.example.TeleprompterAndroid.Consts.CHANGE_MODE;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.CHANGE_SPEED;
import static com.example.TeleprompterAndroid.Consts.CHANGE_TEXT_COLOR;
import static com.example.TeleprompterAndroid.Consts.CHANGE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.PAUSE_MODE;
import static com.example.TeleprompterAndroid.Consts.PLAY_MODE;
import static com.example.TeleprompterAndroid.Consts.STOP_MODE;


public class NewScrollTEXT extends androidx.appcompat.widget.AppCompatTextView implements Runnable {


    private static final float DEFAULT_SPEED = 15.0f;

    private Scroller scroller;
    private float speed = DEFAULT_SPEED;
    private boolean continuousScrolling = true;

    private Handler handler, fandler;


    //constructors
    public NewScrollTEXT(Context context) {
        super(context);
        setup(context);
    }

    public NewScrollTEXT(Context context, AttributeSet attributes) {
        super(context, attributes);
        setup(context);
    }

    private void setup(Context context) {
        scroller = new Scroller(context, new LinearInterpolator());
        setScroller(scroller);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (scroller.isFinished()) {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    switch (msg.arg1) {
                        case CHANGE_SCRIPT:
                            String script = (String) msg.obj;
                            setText(new SpannableString(Html.fromHtml(script, Html.FROM_HTML_MODE_LEGACY)));
                            break;
                        case CHANGE_TEXT_SIZE:
                            int textSize = (int) msg.obj;
                            setTextSize(textSize);
                            break;
                        case CHANGE_SPEED:
                            int speed = (int) msg.obj;
                            setSpeed(speed);
                            break;
                        case CHANGE_MIRRORING:
                            boolean mirroring = (boolean) msg.obj;
                            if (mirroring) mirrorTextOn(); else mirrorTextOff();
                            break;
                        case CHANGE_TEXT_COLOR:
                            String textColor = (String) msg.obj;
                            setTextColor(Color.parseColor(textColor));
                            break;
                        case CHANGE_BACKGROUND_COLOR:
                            String bgColor = (String) msg.obj;
                            setBackgroundColor(Color.parseColor(bgColor));
                            break;
                        case CHANGE_MODE:
                            int mode = (int) msg.obj;
                            switch (mode) {
                                case PAUSE_MODE:
                                    Log.e("SCROLLING_TEXT_VIEW_CLASS", "Should be stopped!");
                                    Message message = new Message();
                                    message.obj = true;
                                    message.what = PAUSE_MODE;
                                    fandler.sendMessage(message);
                                    break;
                                case PLAY_MODE:
                                    Log.e("SCROLLING_TEXT_VIEW_CLASS", "Should be played!");
                                    Message message2 = new Message();
                                    message2.obj = false;
                                    message2.what = PAUSE_MODE;
                                    fandler.sendMessage(message2);
                                    break;
                                case STOP_MODE:
                                    scroller.abortAnimation();
                                    break;
                                default:
                                    Toast.makeText(getContext(), "Change mode: " + mode, Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            break;
                        default:
                            Toast.makeText(getContext(), (int) msg.arg1, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };
            scroll();
        }
    }

    public void changeScript (String script) {
        Message msg = new Message();
        msg.obj = script;
        msg.arg1 = CHANGE_SCRIPT;
        handler.sendMessage(msg);
    }

    public void changeTextSize (int textSize) {
        Message msg = new Message();
        msg.obj = textSize;
        msg.arg1 = CHANGE_TEXT_SIZE;
        handler.sendMessage(msg);
    }

    public void changeMirroring (boolean mirroring) {
        Message msg = new Message();
        msg.obj = mirroring;
        msg.arg1 = CHANGE_MIRRORING;
        handler.sendMessage(msg);
    }

    public void changeSpeed (int speed) {
        Message msg = new Message();
        msg.obj = speed;
        msg.arg1 = CHANGE_SPEED;
        handler.sendMessage(msg);
    }

    public void changeMode (int mode) {
        Message msg = new Message();
        msg.obj = mode;
        msg.arg1 = CHANGE_MODE;
        handler.sendMessage(msg);
    }

    public static int toSpeedValue (int speedGot) {
        return (int) (((100 - speedGot) / 100f) * 49 + 1);
    }

    public static int toPercentValue (int speedGot) {
        return 100 - ((int) ((speedGot - 1) / 49f * 100));
    }

    private boolean pause = false;
    private boolean pauseFlag = false;
    private int pauseY;

    //start Scrolling method
    private void scroll() {
        fandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == PAUSE_MODE) {
                    pause = (boolean) msg.obj;
                    // scroller.forceFinished(true);
                    if (pause) {
                        pauseY = scroller.getCurrY();
                    }
                    scroller.abortAnimation();
                    scroll();
                    // scroller.startScroll(pauseX, pauseOffset, 0, 0, 1);
                }
            }
        };

        int viewHeight, visibleHeight, lineHeight, offset = 0, distance = 0, duration = 0;
        if (!pause) {
            viewHeight = getHeight();
            visibleHeight = viewHeight - getPaddingBottom() - getPaddingTop();
            lineHeight = getLineHeight();

            offset = -1 * visibleHeight;
            distance = visibleHeight + getLineCount() * lineHeight;

            duration = (int) (distance * speed);
        }

        if (pause) {
            if (!pauseFlag) {
                pauseFlag = true;
            }
        }

        if (!pause) {
            if (pauseFlag) {
                scroller.startScroll(0, pauseY, 0, distance, ((int) (distance * speed)));
                pauseFlag = false;
            } else scroller.startScroll(0, offset, 0, distance, duration);
        }
        else scroller.startScroll(0, pauseY, 0, 0, 1000);

        if (continuousScrolling) {
            post(this);
        }
    }

    @Override
    public void run() {
        if (scroller.isFinished()) {
            scroll();
        } else {
            post(this);
        }
    }


    //set speed for Scrolling TextView
    public void setSpeed(float speed) {
        Log.d("SET_SPEED_STVC", "Speed should be changed to " + speed);
        this.speed = speed;
    }

    //get speed for Scrolling TextView
    public float getSpeed() {
        return speed;
    }

    //implementing continius scrolling of text
    public void setContinuousScrolling(boolean continuousScrolling) {
        this.continuousScrolling = continuousScrolling;
    }

    //returns boolean state of scrolling text
    public boolean isContinuousScrolling() {
        return continuousScrolling;
    }


    ///mirror tex
    public void mirrorTextOn() {
        setScaleX(-1);
        setScaleY(1);
    }

    //reverse text to normal
    public void mirrorTextOff() {
        setScaleX(1);
    }

    public void changeTextColor(String textColor) {
        Message msg = new Message();
        msg.obj = textColor;
        msg.arg1 = CHANGE_TEXT_COLOR;
        handler.sendMessage(msg);
    }

    public void changeBackGroundColor(String bgColor) {
        Message msg = new Message();
        msg.obj = bgColor;
        msg.arg1 = CHANGE_BACKGROUND_COLOR;
        handler.sendMessage(msg);
    }
}