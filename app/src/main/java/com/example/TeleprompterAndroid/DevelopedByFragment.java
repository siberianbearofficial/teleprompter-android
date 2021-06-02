package com.example.TeleprompterAndroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DevelopedByFragment extends Fragment {

    public DevelopedByFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_developed_by, container, false);
        layout.findViewById(R.id.alexey_orlov65).setOnClickListener(v -> openVK("alexey_orlov65"));
        layout.findViewById(R.id.business_operator).setOnClickListener(v -> openVK("business_operator"));
        return layout;
    }

    private void openVK (String lastPathSegment) {
        String url = "https://vk.com/" + lastPathSegment;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}