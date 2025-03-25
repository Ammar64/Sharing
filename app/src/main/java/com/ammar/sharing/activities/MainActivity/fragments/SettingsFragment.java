package com.ammar.sharing.activities.MainActivity.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.activities.MainActivity.adaptersR.LanguagesAdapter.LanguagesAdapter;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.ui.NumberDialog;
import com.ammar.sharing.custom.ui.RoundDialog;
import com.ammar.sharing.network.Server;
import com.ammar.sharing.services.ServerService;
import com.suke.widget.SwitchButton;

public class SettingsFragment extends Fragment {
    private View v;

    private ConstraintLayout serverPortCL;
    private CardView uploadDisableCV;
    private CardView usersBlockCV;
    private CardView debugModeCV;
    private SwitchButton uploadDisableS;
    private SwitchButton usersBlockS;
    private SwitchButton debugModeS;

    private ConstraintLayout languageCL;
    private RoundDialog languagesRD;
    private SharedPreferences settingsPref;

    private NumberDialog serverPortND;

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

        serverPortCL = v.findViewById(R.id.CL_ServerPort);
        serverPortND = new NumberDialog(requireContext())
                .setTitle(R.string.server_port)
                .setHint(R.string.port)
                .setMinValue(1024)
                .setMaxValue(65535)
                .setDefaultValue(Server.PORT_NUMBER)
                .setOnResult((port) -> {
                    Server.PORT_NUMBER = port;
                    settingsPref.edit().putInt(Consts.PREF_FIELD_SERVER_PORT, port).apply();
                    Intent intent = new Intent(requireContext(), ServerService.class);
                    intent.setAction(ServerService.ACTION_RESTART_SERVER);
                    requireContext().startService(intent);
                })
                .create();

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
        serverPortCL.setOnClickListener((view) -> {
            serverPortND.setDefaultValue(Server.PORT_NUMBER);
            serverPortND.show();
        });

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
            languagesRD.show();
        });
    }
}
