package com.ammar.filescenter.activities.MainActivity.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.MainActivity;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    public static final String SettingsPrefFile = "SettingsPref";
    public static final String DarkModeKey = "IS_DARK_MODE";
    public static final String UploadDir = "UPLOAD_DIR";

    private View v;
    private Toolbar toolbar;
    private SwitchCompat darkModeSC;


    private SharedPreferences settingsPref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        initItems();
        setItemsListeners();
        return v;
    }

    private void initItems() {
        settingsPref = requireContext().getSharedPreferences(SettingsPrefFile, Context.MODE_PRIVATE);
        toolbar = v.findViewById(R.id.TB_Toolbar);
        toolbar.setTitle("Settings");

        darkModeSC = v.findViewById(R.id.SC_DarkModeToggle);
        darkModeSC.setChecked(settingsPref.getBoolean(DarkModeKey, true));
    }

    private void setItemsListeners() {
        darkModeSC.setOnCheckedChangeListener((view, isChecked) -> {
            if(!settingsPref.edit().putBoolean(DarkModeKey, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
            } else ((MainActivity) requireActivity()).prepareActivity();
        });
    }
}
