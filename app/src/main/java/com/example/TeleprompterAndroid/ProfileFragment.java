package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;
import static com.example.TeleprompterAndroid.Consts.PICK_JPEG_FILE;

public class ProfileFragment extends Fragment {

    private AuthHelper authHelper;
    private FileHelper fileHelper;
    private ImageView avatar;
    private EditText profileName;
    private TextView displayName;
    private ImageButton saveButton;

    private boolean isAuthed;

    public ProfileFragment() {}

    public static ProfileFragment newInstance(boolean isAuthed) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_AUTHED, isAuthed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAuthed = getArguments().getBoolean(IS_AUTHED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_profile, container, false);

        avatar = layout.findViewById(R.id.avatar_main_profile_fragment);
        profileName = layout.findViewById(R.id.name_profile_et);
        displayName = layout.findViewById(R.id.user_displayName_profile_fragment);
        saveButton = layout.findViewById(R.id.button_save_profile_fragment);

        fileHelper = new FileHelper(getActivity());

        if (isAuthed) {
            authHelper = new AuthHelper(getActivity());
            avatar.setOnClickListener(v -> fileHelper.openNewAvatar());
            saveButton.setOnClickListener(v -> {
                authHelper.updateUserName(profileName.getText().toString());
            });

            authHelper.getNameReference().addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    displayName.setText(authHelper.getCurrentProfileName(snapshot));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Database error", error.toString());
                }
            });

            downloadAvatar();

        } else {
            displayName.setText(getString(R.string.guest));
            profileName.setVisibility(View.GONE);
            avatar.setImageResource(R.drawable.guest_avatar);
            saveButton.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction().add(R.id.container_guest_mode_profile_fragment, new GuestFragment()).commit();
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container_guest_mode_profile_fragment, new DevelopedByFragment());
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        return layout;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == PICK_JPEG_FILE) {
                avatar.setImageBitmap((Bitmap) msg.obj);
                uploadAvatar();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        fileHelper.sendFileToHandler(requestCode, resultCode, data, handler);
    }

    private void uploadAvatar() {
        authHelper.getSaveAvatarUploadTask(avatar).addOnFailureListener(exception -> {
            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
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
}