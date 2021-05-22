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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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

            listAllFiles();

        } else {
            userDisplayName.setText(getString(R.string.guest));
            avatar.setImageResource(R.drawable.guest_avatar);
            fragmentManager.beginTransaction().add(R.id.files_container, new GuestFragment()).commit();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.files_container, new DevelopedByFragment());
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }



        fileHelper = new FileHelper(getActivity());

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what != 101010) {
                    startActivity(fileHelper.prepareIntent(msg, textsize, speed));
                    uploadFile();
                } else if (!creditsAdded) {
                    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.files_container, new DevelopedByFragment());
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    creditsAdded = true;
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fileHelper.sendFileToHandler(requestCode, resultCode, data, handler);
    }

    private int listSize;
    private int currNumber;
    private Thread thread;
    private boolean creditsAdded = false;

    private void listAllFiles() {
        authHelper.getFilesReference().listAll()
                .addOnSuccessListener(listResult -> {
                    listSize = listResult.getItems().size();
                    Log.e("ListSize", String.valueOf(listSize));
                    currNumber = 0;
                    for (StorageReference item : listResult.getItems()) {
                        putMetadataInFile(item.getName(), item, new FileFragment(), getChildFragmentManager());
                    }
                    thread = new Thread(() -> {
                        try {
                            if (currNumber == listSize - 1 && !creditsAdded) {
                                Thread.sleep(1000);
                                Message message = new Message();
                                message.what = 101010;
                                handler.sendMessage(message);
                            }
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }); thread.start();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateStared(StorageReference fileReference, boolean stared) {
        fileReference.updateMetadata(new StorageMetadata.Builder().setCustomMetadata(FILE_STAR, stared ? "true" : "false").build())
                .addOnFailureListener((OnFailureListener) exception -> {
                    Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                });
    }

    private void putMetadataInFile(String fileName, StorageReference reference, FileFragment fileFragment, FragmentManager fragmentManager) {
        reference.getMetadata().addOnSuccessListener((OnSuccessListener<StorageMetadata>) storageMetadata -> {
            Bundle args = new Bundle();
            args.putString(FILE_NAME, fileName);
            args.putString(FILE_DATE, new Date(storageMetadata.getUpdatedTimeMillis()).toString());
            args.putBoolean(FILE_STAR, storageMetadata.getCustomMetadata(FILE_STAR).equals("true"));
            args.putInt(FILE_TEXT_SIZE, textsize);
            args.putInt(FILE_SPEED, speed);
            fileFragment.setArguments(args);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.files_container, fileFragment);
            fragmentTransaction.commit();
            currNumber++;
        }).addOnFailureListener((OnFailureListener) exception -> {
            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
        });
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
            updateStared(storageReference, false);
        });
    }

    public void downloadFile (String fileName) throws IOException {
        File localFile = File.createTempFile(fileName, "html");

        authHelper.getFileReference(fileName).getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            try {
                String content = fileHelper.readContentFromFile(localFile);
                ((NewMainActivity) getActivity()).openEditorActivityFragment(fileName, content);
                Log.d("Content", content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
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