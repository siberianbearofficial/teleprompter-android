package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Random;

import static com.example.TeleprompterAndroid.Consts.FILE_DATE;
import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.FILE_SPEED;
import static com.example.TeleprompterAndroid.Consts.FILE_STAR;
import static com.example.TeleprompterAndroid.Consts.FILE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;

public class MainActivityFragment extends Fragment {

    private int textsize;
    private int speed;
    private Handler handler;
    private FileHelper fileHelper;
    private TextView userDisplayName;
    private AuthHelper authHelper;
    private ImageView avatar;

    private boolean isAuthed;

    private boolean is_authed_got = false;

    public MainActivityFragment() {}

    public static MainActivityFragment newInstance(boolean isAuthed) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_AUTHED, isAuthed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            is_authed_got = getArguments().getBoolean(IS_AUTHED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main_activity, container, false);

        textsize = 48;
        speed = 30;

        isAuthed = is_authed_got;

        userDisplayName = layout.findViewById(R.id.user_displayName);
        avatar = layout.findViewById(R.id.avatar_main);

        layout.findViewById(R.id.open_file_from_device_button).setOnClickListener(v -> {
            Upload();
        });

        layout.findViewById(R.id.create_file_button).setOnClickListener(v -> {
            Create();
        });

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        if (isAuthed) {
            authHelper = new AuthHelper(getActivity());

            authHelper.getNameReference().addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userDisplayName.setText(authHelper.getCurrentProfileName(snapshot));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("Database error", error.getMessage());
                }
            });

            //Show all files to open
            int count = 10;
            /*ArrayList<String> fileNames = new ArrayList<>();
            ArrayList<String> fileDates = new ArrayList<>();
            ArrayList<FileFragment> files = new ArrayList<>();*/


            Random random = new Random();
            for (int i = 0; i < count; i++) {
                FileFragment fileFragment = new FileFragment();
                Bundle args = new Bundle();
                args.putString(FILE_NAME, getString(R.string.test_file_name));
                args.putString(FILE_DATE, getString(R.string.test_date));
                args.putBoolean(FILE_STAR, random.nextBoolean());
                args.putInt(FILE_TEXT_SIZE, textsize);
                args.putInt(FILE_SPEED, speed);
                args.putString(FILE_SCRIPT, "Test Script NEW!");
                fileFragment.setArguments(args);
                fragmentTransaction.add(R.id.files_container, fileFragment);
            }
        } else {
            userDisplayName.setText(getString(R.string.guest));
            avatar.setImageResource(R.drawable.guest_avatar);
            fragmentTransaction.add(R.id.files_container, new GuestFragment());
        }

        fragmentTransaction.add(R.id.files_container, new DevelopedByFragment());

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        fileHelper = new FileHelper(getActivity());

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                startActivity(fileHelper.prepareIntent(msg, textsize, speed));
                uploadFile();
            }
        };


        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fileHelper.sendFileToHandler(requestCode, resultCode, data, handler);
    }

    private void uploadFile () {
        Uri file = fileHelper.getFinalUri();
        String fileName = fileHelper.getFileName();
        Log.d("FileName", fileName);
        StorageReference storageReference = authHelper.getFileReference(fileName);
        UploadTask uploadTask = storageReference.putFile(file);

        uploadTask.addOnFailureListener((OnFailureListener) exception -> {
            Toast.makeText(getContext(), "Error! Message: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener((OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {
            Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
        });
    }

    private void BluetoothWriteMode() {
        //startActivity(new Intent(getApplicationContext(), WriteActivity.class).putExtra("SCRIPT", "Test script").putExtra("TEXTSIZE", Integer.toString(textsize)).putExtra("SPEED", Integer.toString(speed)));
        Toast.makeText(getContext(), "Not available feature", Toast.LENGTH_SHORT).show();
    }

    private void Create () {
        //startActivity(new Intent(getContext(), EditorActivityBluetoothDevice.class).putExtra(FILE_SPEED, Integer.toString(speed)).putExtra(FILE_TEXT_SIZE, Integer.toString(textsize)).putExtra(FILE_SCRIPT, "Write something..."));
        ((NewMainActivity) getActivity()).openEditorActivityFragment("Test", "Write this!");
    }

    private void Upload () {fileHelper.openFile();}

    private void Broadcast () {startActivity(new Intent(getContext(), EditorActivity.class).putExtra(FILE_SPEED, Integer.toString(speed)).putExtra(FILE_TEXT_SIZE, Integer.toString(textsize)).putExtra(FILE_SCRIPT, "Write something..."));}

    private void ToSettingsActivity () {startActivity(new Intent(getContext(), SettingsActivity.class));}
}