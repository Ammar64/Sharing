package com.ammar.sharing.activities.MainActivity.fragments;

import static com.ammar.sharing.activities.MainActivity.MainActivity.darkMode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.activities.MainActivity.adaptersR.LanguagesAdapter.LanguagesAdapter;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.ui.RoundDialog;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.suke.widget.SwitchButton;

public class SettingsFragment extends Fragment {
    private View v;

    private CardView uploadDisableCV;
    private CardView usersBlockCV;
    private CardView debugModeCV;
    private SwitchButton uploadDisableS;
    private SwitchButton usersBlockS;
    private SwitchButton debugModeS;

    private ConstraintLayout languageCL;
    private RoundDialog languagesRD;
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

        languageCL = v.findViewById(R.id.CL_SettingsLanguage);

        //String lang = settingsPref.getString(Consts.PREF_FIELD_LANG, "");
        languagesRD = new RoundDialog(requireActivity());
        languagesRD.setView(R.layout.dialog_langauges);
        languagesRD.setCornerRadius((int) Utils.dpToPx(18));

        View languagesDialogView = languagesRD.getView();
        RecyclerView langsRecycler = languagesDialogView.findViewById(R.id.RV_LanguagesRecycler);
        Button langsDialogOkButton = languagesDialogView.findViewById(R.id.B_LanguagesOkButton);

        LanguagesAdapter languagesAdapter = new LanguagesAdapter((MainActivity) requireActivity());
        langsRecycler.setAdapter(languagesAdapter);
        langsRecycler.setItemAnimator(null);

        langsDialogOkButton.setOnClickListener((v) -> {
            languagesRD.dismiss();
            String langCode = languagesAdapter.getSelectedLanguageCode();
            if( langCode != null ) {
                Utils.setLocale(requireActivity(), langCode);
                settingsPref.edit()
                        .putString(Consts.PREF_FIELD_LANG, langCode)
                        .apply();
                requireActivity().recreate();
            }
        });
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

        languageCL.setOnClickListener(view -> {
            Resources res = getResources();
            int color = ResourcesCompat.getColor(res, darkMode ? R.color.dialogColorDark : R.color.dialogColorLight, null);
            languagesRD.setBackgroundColor( color );
            languagesRD.show();
        });
    }
}
