package com.example.TeleprompterAndroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {

    public AboutFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_about, container, false);
        layout.findViewById(R.id.back_button_about_fragment).setOnClickListener(v -> ((NewMainActivity) getActivity()).openSettingsActivityFragment());
        return layout;
    }
}