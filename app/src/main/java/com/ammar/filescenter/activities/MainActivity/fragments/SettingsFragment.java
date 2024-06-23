package com.ammar.filescenter.activities.MainActivity.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.MainActivity;
import com.ammar.filescenter.common.Vals;
import com.ammar.filescenter.common.Utils;

public class SettingsFragment extends Fragment {
    public static final String SettingsPrefFile = "SettingsPref";
    public static final String DarkModeKey = "IS_DARK_MODE";
    public static final String UploadDisable = "UPLOAD_DISABLE";
    public static final String UsersBlock = "USERS_BLOCK";
    public static final String Language = "LANGUAGE";
    private View v;
    private Toolbar toolbar;
    private SwitchCompat darkModeS;

    private SwitchCompat uploadDisableS;
    private SwitchCompat usersBlockS;

    private RelativeLayout languageRL;
    private AlertDialog languageAD;
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
        toolbar.setTitle(R.string.settings);

        darkModeS = v.findViewById(R.id.SC_DarkModeToggle);
        darkModeS.setChecked(settingsPref.getBoolean(DarkModeKey, true));

        uploadDisableS = v.findViewById(R.id.SC_UploadAllowToggle);
        uploadDisableS.setChecked(settingsPref.getBoolean(UploadDisable, false));

        usersBlockS = v.findViewById(R.id.SC_UsersBlockToggle);
        usersBlockS.setChecked(settingsPref.getBoolean(UsersBlock, false));

        languageRL = v.findViewById(R.id.RL_SettingsLanguage);

        String lang = settingsPref.getString(Language, "");
        int selected = Vals.langsCode.indexOf(lang);
        languageAD = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_language)
                .setSingleChoiceItems(R.array.langs, selected, null)
                .setPositiveButton(R.string.ok, (dialog, ignore) -> {
                    ListView lw = languageAD.getListView();
                    if (lw.getCheckedItemCount() > 0) {
                        int which = lw.getCheckedItemPosition();
                        settingsPref.edit()
                                .putString(Language, Vals.langsCode.get(which))
                                .apply();
                        Utils.setLocale((MainActivity) requireActivity(), Vals.langsCode.get(which));
                        requireActivity().recreate();
                    }
                })
                .create();
    }

    private void setItemsListeners() {
        darkModeS.setOnCheckedChangeListener((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(DarkModeKey, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
            } else ((MainActivity) requireActivity()).prepareActivity();
        });

        uploadDisableS.setOnCheckedChangeListener((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(UploadDisable, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
            }
        });

        usersBlockS.setOnCheckedChangeListener((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(UsersBlock, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
            }
        });

        languageRL.setOnClickListener(view -> languageAD.show());
    }

}
