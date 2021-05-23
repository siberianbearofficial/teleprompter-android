package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static com.example.TeleprompterAndroid.Consts.CHANGE_SPEED;
import static com.example.TeleprompterAndroid.Consts.CHANGE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.FILE_DATE;
import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.FILE_SPEED;
import static com.example.TeleprompterAndroid.Consts.FILE_STAR;
import static com.example.TeleprompterAndroid.Consts.FILE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.STARED_SUCCESS;

public class FileFragment extends Fragment {

    private String name;
    private String date;
    private String script;
    private int speed;
    private int textSize;
    private boolean star;

    public FileFragment() {}

    public static FileFragment newInstance(String name, String date, boolean star, int textSize, int speed, String script) {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        args.putString(FILE_NAME, name);
        args.putString(FILE_DATE, date);
        args.putString(FILE_SCRIPT, script);
        args.putBoolean(FILE_STAR, star);
        args.putInt(FILE_TEXT_SIZE, textSize);
        args.putInt(FILE_SPEED, speed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle arguments = getArguments();
            name = arguments.getString(FILE_NAME);
            date = arguments.getString(FILE_DATE);
            script = arguments.getString(FILE_SCRIPT);
            star = arguments.getBoolean(FILE_STAR);
            speed = arguments.getInt(FILE_SPEED);
            textSize = arguments.getInt(FILE_TEXT_SIZE);
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == STARED_SUCCESS) {
                updateStarIV();
            } else {
                Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                star = !star;
                updateStarIV();
            }
        }
    };

    private ImageView starIV;

    private void updateStarIV() {
        starIV.setImageResource(star ? R.drawable.filled_star_icon : R.drawable.star_icon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_file, container, false);
        TextView nameTV = layout.findViewById(R.id.file_name), dateTV = layout.findViewById(R.id.file_date);
        starIV = layout.findViewById(R.id.file_star);
        nameTV.setText(name); dateTV.setText(date);
        if (star) starIV.setImageResource(R.drawable.filled_star_icon);
        starIV.setOnClickListener(v -> {
            star = !star;
            ((MainActivityFragment) getParentFragment()).updateStared(name, handler, star);
        });
        nameTV.setOnClickListener(v -> {
            try {
                ((MainActivityFragment) getParentFragment()).downloadFile(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //startActivity(new Intent(getContext(), WriteActivity.class).putExtra("SCRIPT", script).putExtra("TEXTSIZE", Integer.toString(textSize)).putExtra("SPEED", Integer.toString(speed)));
        });
        return layout;
    }
}