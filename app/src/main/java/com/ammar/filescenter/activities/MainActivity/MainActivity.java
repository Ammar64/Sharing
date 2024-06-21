package com.ammar.filescenter.activities.MainActivity;

import android.Manifest;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.fragments.SettingsFragment;
import com.ammar.filescenter.activities.MainActivity.fragments.TransferFragment;
import com.ammar.filescenter.common.Abbrev;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.NetworkService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton serverButton;
    private BottomNavigationView bottomNavigationView;

    public static boolean darkMode;
    public final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    public final int REQUEST_CODE_NOTIFICATION_PERMISSION = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        prepareActivity();
        super.onCreate(savedInstanceState);
        requestPermissions();
        setContentView(R.layout.activity_main);
        initItems();
        setItemsListener();
        initStates();
        observeStates();
    }

    public void prepareActivity() {
        SharedPreferences settingsPref = getSharedPreferences(SettingsFragment.SettingsPrefFile, MODE_PRIVATE);
        // check for first Run
        SharedPreferences firstRunPref = getSharedPreferences("FirstRun", MODE_PRIVATE);
        boolean isFirstRun = firstRunPref.getBoolean("firstrun", true);
        if (isFirstRun) {
            settingsPref.edit()
                    .putBoolean(SettingsFragment.DarkModeKey, true)
                    .apply();
            firstRunPref.edit().putBoolean("firstrun", false).apply();
        }
        darkMode = settingsPref.getBoolean(SettingsFragment.DarkModeKey, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
            uiModeManager.setApplicationNightMode(darkMode ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        }

        String lang = settingsPref.getString(SettingsFragment.Language, "");
        if (!lang.isEmpty()) {
            Utils.setLocale(this, lang);
        }
    }

    private void initItems() {
        bottomNavigationView = findViewById(R.id.BottomNavView);
        serverButton = findViewById(R.id.FAB_ServerButton);
        bottomNavigationView.setSelectedItemId(R.id.B_Share);
    }


    private int currentFragmentIndex = 1;
    private final ArrayList<Class> fragments = new ArrayList<>(Arrays.asList(new Class[]{
            TransferFragment.class,
            SettingsFragment.class
    }));


    private void setItemsListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();

            int nextFragmentIndex = -1;
            if (id == R.id.B_Share) {
                nextFragmentIndex = fragments.indexOf(TransferFragment.class);
                if (currentFragmentIndex < nextFragmentIndex) {
                    ft.setCustomAnimations(R.anim.fragment_enter_left, R.anim.fragment_exit_left);
                }
                ft.replace(R.id.MainActivityFragmentContainer, TransferFragment.class, null);
            } else if (id == R.id.B_Settings) {
                nextFragmentIndex = fragments.indexOf(TransferFragment.class);
                if (currentFragmentIndex < nextFragmentIndex) {
                    ft.setCustomAnimations(R.anim.fragment_enter_left, R.anim.fragment_exit_left);
                }
                ft.replace(R.id.MainActivityFragmentContainer, SettingsFragment.class, null);
            }
            currentFragmentIndex = nextFragmentIndex;
            ft.commit();
            return true;
        });
        serverButton.setOnClickListener((button) -> {
            Intent serviceIntent = new Intent(this, NetworkService.class);
            serviceIntent.setAction(Abbrev.ACTION_TOGGLE_SERVER);
            startService(serviceIntent);
        });
    }

    private void initStates() {
        Intent serviceIntent = new Intent(this, NetworkService.class);
        serviceIntent.setAction(Abbrev.ACTION_GET_SERVER_STATUS);
        startService(serviceIntent);
    }

    private void observeStates() {
        NetworkService.serverStatusObserver.observe(this, running -> {
            if (running) {
                serverButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.status_on)));
            } else {
                serverButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.status_off)));
            }
        });
    }

    private void requestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Uri uri = Uri.parse(String.format(Locale.ENGLISH, "package:%s", getApplicationContext().getPackageName()));
                startActivity(
                        new Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                uri
                        )
                );

            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_CODE_STORAGE_PERMISSION);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.POST_NOTIFICATIONS
            }, REQUEST_CODE_NOTIFICATION_PERMISSION);
        }
    }


}