package com.ammar.sharing.activities.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.adaptersR.LanguagesAdapter.LanguagesAdapter;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.ui.DefaultActivity;
import com.ammar.sharing.custom.ui.NumberDialog;
import com.ammar.sharing.custom.ui.RoundDialog;
import com.ammar.sharing.network.Server;
import com.ammar.sharing.services.ServerService;
import com.suke.widget.SwitchButton;

public class SettingsActivity extends DefaultActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initItems();
        setItemsListeners();
    }

    private ConstraintLayout serverPortCL;
    private CardView enableHTTPSCV;
    private CardView uploadDisableCV;
    private CardView usersBlockCV;
    private CardView debugModeCV;
    private SwitchButton enableHTTPSS;
    private SwitchButton uploadDisableS;
    private SwitchButton usersBlockS;
    private SwitchButton debugModeS;

    private ConstraintLayout languageCL;
    private RoundDialog languagesRD;
    private SharedPreferences settingsPref;

    private NumberDialog serverPortND;

    private void initItems() {
        Toolbar appBarTB = findViewById(R.id.TB_Toolbar);
        setSupportActionBar(appBarTB);
        appBarTB.setNavigationIcon(R.drawable.ic_back);
        setTitle(R.string.settings);

        settingsPref = getSharedPreferences(Consts.PREF_SETTINGS, Context.MODE_PRIVATE);
        serverPortCL = findViewById(R.id.CL_ServerPort);
        serverPortND = new NumberDialog(this)
                .setTitle(R.string.server_port)
                .setHint(R.string.port)
                .setMinValue(1024)
                .setMaxValue(65535)
                .setDefaultValue(Server.PORT_NUMBER)
                .setOnResult((port) -> {
                    Server.PORT_NUMBER = port;
                    settingsPref.edit().putInt(Consts.PREF_FIELD_SERVER_PORT, port).apply();
                    Intent intent = new Intent(this, ServerService.class);
                    intent.setAction(ServerService.ACTION_RESTART_SERVER);
                    startService(intent);
                })
                .create();

        enableHTTPSCV = findViewById(R.id.CV_EnableHTTPS);
        uploadDisableCV = findViewById(R.id.CV_UploadDisable);
        usersBlockCV = findViewById(R.id.CV_UserBlock);
        debugModeCV = findViewById(R.id.CV_DebugMode);

        enableHTTPSS = findViewById(R.id.SC_EnableHTTPSToggle);
        enableHTTPSS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_IS_HTTPS, true));

        uploadDisableS = findViewById(R.id.SC_UploadAllowToggle);
        uploadDisableS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, false));

        usersBlockS = findViewById(R.id.SC_UsersBlockToggle);
        usersBlockS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_ARE_USERS_BLOCKED, false));

        debugModeS = findViewById(R.id.SC_DebugModeToggle);
        debugModeS.setChecked(settingsPref.getBoolean(Consts.PREF_FIELD_DEBUG_MODE, false));

        languageCL = findViewById(R.id.CL_SettingsLanguage);

        //String lang = settingsPref.getString(Consts.PREF_FIELD_LANG, "");
        languagesRD = new RoundDialog(this);
        languagesRD.setView(R.layout.dialog_langauges);
        languagesRD.setCornerRadius((int) Utils.dpToPx(18));

        View languagesDialogView = languagesRD.getView();
        RecyclerView langsRecycler = languagesDialogView.findViewById(R.id.RV_LanguagesRecycler);
        Button langsDialogOkButton = languagesDialogView.findViewById(R.id.B_LanguagesOkButton);

        LanguagesAdapter languagesAdapter = new LanguagesAdapter(this);
        langsRecycler.setAdapter(languagesAdapter);
        langsRecycler.setItemAnimator(null);

        langsDialogOkButton.setOnClickListener((v) -> {
            languagesRD.dismiss();
            String langCode = languagesAdapter.getSelectedLanguageCode();
            if( langCode != null ) {
                Utils.setLocale(this, langCode);
                settingsPref.edit()
                        .putString(Consts.PREF_FIELD_LANG, langCode)
                        .apply();
                recreate();
            }
        });
    }

    private void setItemsListeners() {
        serverPortCL.setOnClickListener((view) -> {
            serverPortND.setDefaultValue(Server.PORT_NUMBER);
            serverPortND.show();
        });

        enableHTTPSCV.setOnClickListener((view) -> enableHTTPSS.toggle());
        enableHTTPSS.setOnCheckedChangeListener((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_IS_HTTPS, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change IS_HTTPS value");
                enableHTTPSS.setChecked(!isChecked);
            } else {
                Server.IS_HTTPS = isChecked;
            }
        });

        uploadDisableCV.setOnClickListener((view) -> uploadDisableS.toggle());
        uploadDisableS.setOnCheckedChangeListener(((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change IS_UPLOAD_DISABLED value");
                uploadDisableS.setChecked(!isChecked);
            }
        }));

        usersBlockCV.setOnClickListener((view) -> usersBlockS.toggle());
        usersBlockS.setOnCheckedChangeListener(((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_ARE_USERS_BLOCKED, isChecked).commit()) {
                Log.e("MYLOG", "Failed to change ARE_USERS_BLOCKED value");
                usersBlockS.setChecked(!isChecked);
            }
        }));

        debugModeCV.setOnClickListener((view) -> debugModeS.toggle());
        debugModeS.setOnCheckedChangeListener((view, isChecked) -> {
            if (!settingsPref.edit().putBoolean(Consts.PREF_FIELD_DEBUG_MODE, isChecked).commit()) {
                Toast.makeText(this, "Failed to change DEBUG_MODE value", Toast.LENGTH_SHORT).show();
                debugModeS.setChecked(!isChecked);
            }
           recreate();
        });

        languageCL.setOnClickListener(view -> languagesRD.show());
    }
}
