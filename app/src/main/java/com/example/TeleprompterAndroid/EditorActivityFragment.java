package com.example.TeleprompterAndroid;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.AREditor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.FILE_STAR;
import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;

public class EditorActivityFragment extends Fragment {

    private String script, title;
    private AREditor arEditor;
    private ImageButton PlayButton, SaveButton, ShareButton, BackButton;
    private TextView TitleTV;

    private boolean saveToServer;

    public EditorActivityFragment() {}

    public static EditorActivityFragment newInstance(String title, String script, boolean isAuthed) {
        EditorActivityFragment fragment = new EditorActivityFragment();
        Bundle args = new Bundle();
        args.putString(FILE_NAME, title);
        args.putString(FILE_SCRIPT, script);
        args.putBoolean(IS_AUTHED, isAuthed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            saveToServer = getArguments().getBoolean(IS_AUTHED);
            script = getArguments().getString(FILE_SCRIPT);
            title = getArguments().getString(FILE_NAME);
        } else {
            saveToServer = false;
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
        String titleShortened = title;
        if (title.length() > 8) {
            titleShortened = title.substring(0, 5) + "...";
        }
        TitleTV.setText(titleShortened);

        SaveButton.setOnClickListener(v -> {
            if (saveToServer) {
                uploadFile(FileHelper.writeContentToInputStream(arEditText.getHtml()), title);
            } else {
                Uri uri = ((NewMainActivity) requireActivity()).getUriForCreatingFile();
                FileHelper.writeScriptToUri(arEditText.getHtml(), uri, getActivity());
            }
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
        });
        PlayButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getContext());
            dialog.setTitle(getString(R.string.file_creation));
            dialog.setContentView(R.layout.dialog_choose_play_type);

            Button playButton = dialog.findViewById(R.id.on_this_device_choose_dialog);
            Button broadcastButton = dialog.findViewById(R.id.broadcast_choose_dialog);

            playButton.setOnClickListener(v1 -> {
                ((NewMainActivity) requireActivity()).openPlayActivityFragment(arEditText.getHtml());
                dialog.dismiss();
            });
            broadcastButton.setOnClickListener(v1 -> {
                ((NewMainActivity) requireActivity()).openWriteActivityFragment(arEditText.getHtml());
                dialog.dismiss();
            });

            dialog.show();
        });
        ShareButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Unavailable feature", Toast.LENGTH_SHORT).show();
        });
        BackButton.setOnClickListener(v -> {
            ((NewMainActivity) requireActivity()).openMainActivityFragment();
        });

        return layout;
    }

    private void uploadFile (InputStream file, String fileName) {
        AuthHelper authHelper = ((NewMainActivity) requireActivity()).getAuthHelper();
        StorageReference storageReference = authHelper.getFileReference(fileName);
        UploadTask uploadTask = storageReference.putStream(file);
        uploadTask.addOnFailureListener(exception -> {
            Toast.makeText(getContext(), "Error! Message: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
            updateStared(storageReference, false);
        });
    }

    private void updateStared(StorageReference fileReference, boolean stared) {
        fileReference.updateMetadata(new StorageMetadata.Builder().setCustomMetadata(FILE_STAR, stared ? "true" : "false").build())
                .addOnFailureListener((OnFailureListener) exception -> {
                    Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                });
    }
}