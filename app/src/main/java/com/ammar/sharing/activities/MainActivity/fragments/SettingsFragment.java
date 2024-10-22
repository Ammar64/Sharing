package com.ammar.sharing.activities.MainActivity.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.Utils;
import com.suke.widget.SwitchButton;

public class SettingsFragment extends Fragment {
    private View v;

    private CardView uploadDisableCV;
    private CardView usersBlockCV;
    private CardView debugModeCV;
    private SwitchButton uploadDisableS;
    private SwitchButton usersBlockS;
    private SwitchButton debugModeS;

    private ConstraintLayout languageRL;
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

        uploadDisableCV = v.findViewById(R.id.CV_UploadDisable);
        usersBlockCV = v.findViewById(R.id.CV_UserBlock);
        debugModeCV = v.findViewById(R.id.CV_DebugMode);

        uploadDisableS = v.findViewById(R.id.SC_UploadAllowToggle);
        uploadDisableS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, false));
        usersBlockS = v.findViewById(R.id.SC_UsersBlockToggle);
        usersBlockS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_ARE_USER_BLOCKED, false));

        debugModeS = v.findViewById(R.id.SC_DebugModeToggle);
        debugModeS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_DEBUG_MODE, false));

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
                        Utils.setLocale(requireActivity(), Consts.langsCode.get(which));
                        requireActivity().recreate();
                    }
                })
                .create();

    }

    private void setItemsListeners() {


        uploadDisableCV.setOnClickListener((view) -> {
            uploadDisableS.toggle();
        });

        uploadDisableS.setOnCheckedChangeListener(((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
                uploadDisableS.setChecked(!isChecked);
            }
        }));

        usersBlockCV.setOnClickListener((view) -> {
            usersBlockS.toggle();
        });

        usersBlockS.setOnCheckedChangeListener(((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_ARE_USER_BLOCKED, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change dark mode");
                usersBlockS.setChecked(!isChecked);
            }
        }));

        debugModeCV.setOnClickListener((view) -> {
            debugModeS.toggle();
        });

        debugModeS.setOnCheckedChangeListener((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_DEBUG_MODE, isChecked).commit()) {
                Toast.makeText(requireContext(), "Debug mode failed to toggle", Toast.LENGTH_SHORT).show();
                debugModeS.setChecked(!isChecked);
            }
            requireActivity().recreate();
        });
        languageRL.setOnClickListener(view -> {
            // Dialog is dark but text is also dark that's the problem
//            Window window = languageAD.getWindow();
//            if( window != null )
//                window.setBackgroundDrawableResource( darkMode ? R.color.dialogColorDark : R.color.dialogColorLight );
            languageAD.show();
        });
    }
}
