package com.example.TeleprompterAndroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NoDocumentsFragment extends Fragment {

    public NoDocumentsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_no_documents, container, false);
        layout.findViewById(R.id.no_documents_fragment_main_text).setOnClickListener(v -> ((NewMainActivity) requireActivity()).openAboutFragment());
        return layout;
    }

}