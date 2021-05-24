package com.example.TeleprompterAndroid;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.TeleprompterAndroid.Consts.CREATE_HTML_FILE;
import static com.example.TeleprompterAndroid.Consts.FILE_DATE;
import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.FILE_SPEED;
import static com.example.TeleprompterAndroid.Consts.FILE_STAR;
import static com.example.TeleprompterAndroid.Consts.FILE_TEXT_SIZE;
import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;
import static com.example.TeleprompterAndroid.Consts.PICK_HTML_FILE;
import static com.example.TeleprompterAndroid.Consts.STARED_FAIL;
import static com.example.TeleprompterAndroid.Consts.STARED_SUCCESS;

public class MainActivityFragment extends Fragment {

    private int textsize;
    private int speed;
    private Handler handler;
    private FileHelper fileHelper;
    private TextView userDisplayName;
    private AuthHelper authHelper;
    private ImageView avatar;

    private Dialog dialog;

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

        setupChooseFileDialog();

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

            layout.findViewById(R.id.sign_out_button).setOnClickListener(v -> {
                authHelper.signOut();
                startActivity(new Intent(getContext(), StartActivity.class));
            });

            downloadAvatar();

            //Show all files to open

            listAllFiles();

        } else {
            userDisplayName.setText(getString(R.string.guest));
            avatar.setImageResource(R.drawable.guest_avatar);
            layout.findViewById(R.id.sign_out_button).setVisibility(View.GONE);
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
                if (msg.what == PICK_HTML_FILE) {
                    //startActivity(fileHelper.prepareIntent(msg, textsize, speed));
                    Bundle arguments = fileHelper.getArguments(msg);
                    Dialog dialog = new Dialog(getContext());
                    dialog.setTitle(R.string.should_save_to_server);
                    dialog.setContentView(R.layout.dialog_should_save_to_server);
                    Uri uri = fileHelper.getFinalUri();
                    dialog.findViewById(R.id.save_and_open_button).setOnClickListener(v -> {
                        arguments.putBoolean(IS_AUTHED, true);
                        ((NewMainActivity) getActivity()).openEditorActivityFragment(arguments);
                        uploadFile(uri, arguments.getString(FILE_NAME));
                        dialog.dismiss();
                    });
                    dialog.findViewById(R.id.just_open_button).setOnClickListener(v -> {
                        arguments.putBoolean(IS_AUTHED, false);
                        ((NewMainActivity) getActivity()).setUriForCreatingFile(uri);
                        ((NewMainActivity) getActivity()).openEditorActivityFragment(arguments);
                        dialog.dismiss();
                    });
                    dialog.show();
                } else if (msg.what == CREATE_HTML_FILE) {
                    //TODO: Open editor fragment with new created file
                    ((NewMainActivity) getActivity()).setUriForCreatingFile(fileHelper.getFinalUri());
                } else {
                    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.files_container, new DevelopedByFragment());
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
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

    private void listAllFiles() {
        authHelper.getFilesReference().listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        putMetadataInFile(item.getName(), item, new FileFragment(), getChildFragmentManager());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void updateStared(StorageReference fileReference, boolean stared) {
        fileReference.updateMetadata(new StorageMetadata.Builder().setCustomMetadata(FILE_STAR, stared ? "true" : "false").build())
                .addOnFailureListener((OnFailureListener) exception -> {
                    Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                });
    }

    private void downloadAvatar() {
        final long ONE_GYGABYTE = 1024 * 1024 * 1024;

        authHelper.getAvatarReference().getBytes(ONE_GYGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            avatar.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
        });

    }

    public void updateStared(String fileName, Handler handler, boolean stared) {
        authHelper.getFileReference(fileName).updateMetadata(new StorageMetadata.Builder().setCustomMetadata(FILE_STAR, stared ? "true" : "false").build())
                .addOnSuccessListener(storageMetadata -> {
                    Message message = new Message();
                    message.what = STARED_SUCCESS;
                    handler.sendMessage(message);
                })
                .addOnFailureListener((OnFailureListener) exception -> {
                    Message message = new Message();
                    message.what = STARED_FAIL;
                    handler.sendMessage(message);
                });
    }

    private void putMetadataInFile(String fileName, StorageReference reference, FileFragment fileFragment, FragmentManager fragmentManager) {
        reference.getMetadata().addOnSuccessListener((OnSuccessListener<StorageMetadata>) storageMetadata -> {
            Bundle args = new Bundle();
            args.putString(FILE_NAME, fileName);
            args.putString(FILE_DATE, new SimpleDateFormat("dd.MM.yy").format(new Date(storageMetadata.getUpdatedTimeMillis())));
            args.putBoolean(FILE_STAR, Objects.equals(storageMetadata.getCustomMetadata(FILE_STAR), "true"));
            args.putInt(FILE_TEXT_SIZE, textsize);
            args.putInt(FILE_SPEED, speed);
            fileFragment.setArguments(args);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.files_container, fileFragment);
            fragmentTransaction.commit();
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

    private void uploadFile (Uri file, String fileName) {
        StorageReference storageReference = authHelper.getFileReference(fileName);
        UploadTask uploadTask = storageReference.putFile(file);

        uploadTask.addOnFailureListener((OnFailureListener) exception -> {
            //Toast.makeText(getContext(), "Error! Message: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener((OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {
            //Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
            updateStared(storageReference, false);
        });
    }

    private void setupChooseFileDialog () {
        dialog = new Dialog(getContext());

        dialog.setTitle(getString(R.string.file_creation));

        dialog.setContentView(R.layout.dialog_choose_file_location);

        AtomicBoolean checked = new AtomicBoolean(true);

        EditText fileName = dialog.findViewById(R.id.file_name_edit_text);
        RadioGroup fileLocationRadioGroup = dialog.findViewById(R.id.file_location_radio_group);
        Button chooseFileLocationButton = dialog.findViewById(R.id.choose_file_location_button);
        Button createFileButton = dialog.findViewById(R.id.create_file_button_dialog);

        fileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (checked.get())
                    createFileButton.setVisibility(View.VISIBLE);
            }
        });

        fileLocationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.account_radio_button:
                    checked.set(true);
                    chooseFileLocationButton.setVisibility(View.GONE);
                    break;
                case R.id.disc_radio_button:
                    checked.set(false);
                    chooseFileLocationButton.setVisibility(View.VISIBLE);
                    createFileButton.setVisibility(View.GONE);
                    break;
            }
        });

        chooseFileLocationButton.setOnClickListener(v -> {
            chooseFileLocationButton.setVisibility(View.GONE);
            createFileButton.setVisibility(View.VISIBLE);
            fileHelper.createFile(fileName.getText().toString()+".html");
        });

        createFileButton.setOnClickListener(v -> {
            if (checked.get())
                ((NewMainActivity) getActivity()).openEditorActivityFragmentForServerFile(fileName.getText().toString()+".html");
            else ((NewMainActivity) getActivity()).openEditorActivityFragmentWithFile();
            dialog.dismiss();
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
        //((NewMainActivity) getActivity()).openEditorActivityFragment("Test", "Write this!");
        dialog.show();
    }

    private void Upload () {fileHelper.openFile();}

    private void Broadcast () {startActivity(new Intent(getContext(), EditorActivity.class).putExtra(FILE_SPEED, Integer.toString(speed)).putExtra(FILE_TEXT_SIZE, Integer.toString(textsize)).putExtra(FILE_SCRIPT, "Write something..."));}

    private void ToSettingsActivity () {startActivity(new Intent(getContext(), SettingsActivity.class));}
}