package com.example.TeleprompterAndroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamadevelopment.scrolltextview.ScrollTextView;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class PlayActivity extends AppCompatActivity {

    private NewScrollTEXT scrollTextView;
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

/*    @Override
    public void onBackPressed() {
//        startActivity(new Intent(getApplicationContext(), NewMainActivity.class));
      textSize = (Flowable<Integer>) changeSize().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
          @Override
          public void accept(Integer integer) throws Exception {

          }
      });
    }*/

    @Override
    public void onBackPressed() {
        Message msg = new Message();
        msg.obj = 20;
        scrollTextView.changeHandlerMessage(20);
    }

    public Flowable<Integer> changeSize() {
        return Flowable.just(20);
    }

    //Flowable<Integer> textSize = Flowable.just(10);
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
        //scrollTextView.setTextSize(textSize);




        scrollTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.test_font));

        scrollTextView.stopNestedScroll();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        container.setPadding(30, 0, 30, 20);
        scrollTextView.setLayoutParams(layoutParams);
        container.removeAllViews();
        container.addView(scrollTextView);
    }
}