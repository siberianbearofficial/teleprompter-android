package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GuestFragment extends Fragment {

    public GuestFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_guest, container, false);

        layout.findViewById(R.id.please_sign_up).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), RegisterActivity.class));
        });
        layout.findViewById(R.id.please_sign_in).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), StartActivity.class));
        });
        layout.findViewById(R.id.guest_fragment_main_text).setOnClickListener(v -> {
            ((NewMainActivity) requireActivity()).openAboutFragment();
        });

        return layout;
    }
}