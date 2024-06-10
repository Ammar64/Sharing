package com.ammar.filescenter.activities.MainActivity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    private View v;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        initItems();
        return v;
    }

    private void initItems() {
        toolbar = v.findViewById(R.id.TB_Toolbar);
        toolbar.setTitle("Settings");



    }
}
