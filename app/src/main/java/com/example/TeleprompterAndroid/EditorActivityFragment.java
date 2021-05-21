package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.AREditor;

import org.w3c.dom.Text;

import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;

public class EditorActivityFragment extends Fragment {

    private String script, title;
    private AREditor arEditor;
    private ImageButton PlayButton, SaveButton, ShareButton, BackButton;
    private TextView TitleTV;

    public EditorActivityFragment() {}

    public static EditorActivityFragment newInstance(String param1, String param2) {
        EditorActivityFragment fragment = new EditorActivityFragment();
        Bundle args = new Bundle();
        args.putString(FILE_SCRIPT, param1);
        args.putString(FILE_NAME, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            script = getArguments().getString(FILE_SCRIPT);
            title = getArguments().getString(FILE_NAME);
        } else {
            script = "";
            title = "New file";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_editor_activity, container, false);
        arEditor = layout.findViewById(R.id.areditor_editor_fragment);

        SaveButton = layout.findViewById(R.id.save_text_button_on_editor_fragment);
        PlayButton = layout.findViewById(R.id.play_button_on_editor_fragment);
        ShareButton = layout.findViewById(R.id.share_button_on_editor_fragment);
        BackButton = layout.findViewById(R.id.back_button_on_editor_fragment);

        TitleTV = layout.findViewById(R.id.title_tv_on_editor_fragment);

        arEditor.setExpandMode(AREditor.ExpandMode.FULL);
        arEditor.setHideToolbar(false);
        arEditor.setToolbarAlignment(AREditor.ToolbarAlignment.BOTTOM);
        AREditText arEditText = arEditor.getARE();
        arEditText.setText(Html.fromHtml(script, Html.FROM_HTML_MODE_LEGACY));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arEditText.setTypeface(getResources().getFont(R.font.montserrat_alternates_light));
        }

        TitleTV.setText(title);

        SaveButton.setOnClickListener(v -> {

        });
        PlayButton.setOnClickListener(v -> {

        });
        ShareButton.setOnClickListener(v -> {

        });
        BackButton.setOnClickListener(v -> {

        });

        return layout;
    }
}