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
import androidx.fragment.app.Fragment;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.MainActivity;
import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.common.Utils;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {
    private View v;

    private RelativeLayout uploadDisableRL;
    private RelativeLayout usersBlockRL;
    private SwitchMaterial uploadDisableS;
    private SwitchMaterial usersBlockS;

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
        settingsPref = requireContext().getSharedPreferences(Consts.PREF_SETTINGS, Context.MODE_PRIVATE);

        uploadDisableRL = v.findViewById(R.id.RL_UploadDisable);
        usersBlockRL = v.findViewById(R.id.RL_UserBlock);

        uploadDisableS = v.findViewById(R.id.SC_UploadAllowToggle);
        uploadDisableS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, false));

        usersBlockS = v.findViewById(R.id.SC_UsersBlockToggle);
        usersBlockS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_ARE_USER_BLOCKED, false));

        languageRL = v.findViewById(R.id.RL_SettingsLanguage);

        String lang = settingsPref.getString(Consts.PREF_FIELD_LANG, "");
        int selected = Consts.langsCode.indexOf(lang);
        languageAD = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_language)
                .setSingleChoiceItems(R.array.langs, selected, null)
                .setPositiveButton(R.string.ok, (dialog, ignore) -> {
                    ListView lw = languageAD.getListView();
                    if (lw.getCheckedItemCount() > 0) {
                        int which = lw.getCheckedItemPosition();
                        settingsPref.edit()
                                .putString(Consts.PREF_FIELD_LANG, Consts.langsCode.get(which))
                                .apply();
                        Utils.setLocale((MainActivity) requireActivity(), Consts.langsCode.get(which));
                        requireActivity().recreate();
                    }
                })
                .create();
    }

    private void setItemsListeners() {


        uploadDisableRL.setOnClickListener((view) -> {
            boolean isChecked = uploadDisableS.isChecked();
            uploadDisableS.setChecked(!isChecked);
            boolean newValue = !isChecked;
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, newValue).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
            }
        });

        usersBlockRL.setOnClickListener((view) -> {
            boolean isChecked = usersBlockS.isChecked();
            usersBlockS.setChecked(!isChecked);
            boolean newValue = !isChecked;

            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_ARE_USER_BLOCKED, newValue).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
            }
        });

        languageRL.setOnClickListener(view -> {
            //languageAD.getWindow().setBackgroundDrawableResource( darkMode ? R.color.dialogColorDark : R.color.dialogColorLight );
            languageAD.show();
        });
    }
}
