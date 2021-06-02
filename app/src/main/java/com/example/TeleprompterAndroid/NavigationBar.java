package com.example.TeleprompterAndroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Objects;

public class NavigationBar extends Fragment {

    private ImageButton homeButton, profileButton, playButton, settingsButton;

    public NavigationBar() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navigation_bar, container, false);
        homeButton = layout.findViewById(R.id.navigation_bar_home);
        profileButton = layout.findViewById(R.id.navigation_bar_profile);
        playButton = layout.findViewById(R.id.navigation_bar_play);
        settingsButton = layout.findViewById(R.id.navigation_bar_settings);

        colorButton(0);

        homeButton.setOnClickListener(v -> {
            colorButton(0);
            ((NewMainActivity) requireActivity()).openMainActivityFragment();
        });
        profileButton.setOnClickListener(v -> {
            colorButton(1);
            ((NewMainActivity) requireActivity()).openProfileFragment();
        });
        playButton.setOnClickListener(v -> {
            colorButton(2);
            ((NewMainActivity) requireActivity()).openReadActivityFragment();
        });
        settingsButton.setOnClickListener(v -> {
            colorButton(3);
            ((NewMainActivity) requireActivity()).openSettingsActivityFragment();
        });

        return layout;
    }

    private void colorButton (int number) {
        homeButton.setImageResource(number == 0 ? R.drawable.home_button_green : R.drawable.home_button_blue);
        profileButton.setImageResource(number == 1 ? R.drawable.profile_button_green : R.drawable.profile_button_blue);
        playButton.setImageResource(number == 2 ? R.drawable.play_button_green : R.drawable.play_button_blue);
        settingsButton.setImageResource(number == 3 ? R.drawable.settings_button_green : R.drawable.settings_button_blue);
    }
}