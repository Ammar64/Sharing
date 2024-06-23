package com.ammar.filescenter.activities.MainActivity;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.fragments.SettingsFragment;
import com.ammar.filescenter.activities.TutorialActivity.TutorialActivity;
import com.ammar.filescenter.application.FilesCenterApp;
import com.ammar.filescenter.common.Data;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.common.Vals;
import com.ammar.filescenter.services.NetworkService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton serverButton;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private AlertDialog errorDialogAD;

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
            startActivity(new Intent(this, TutorialActivity.class));
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
        viewPager = findViewById(R.id.MainActivityFragmentContainer);
        bottomNavigationView.setSelectedItemId(R.id.B_Share);

        MainViewPagerAdapter viewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(viewPagerAdapter);

        errorDialogAD = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.ok, null)
                .create();
    }

    private void setItemsListener() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private MenuItem prevMenuItem;

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                if (position == 1) position = 2;
                Log.d("page", "onPageSelected: " + position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

            }


        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.B_Share) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (id == R.id.B_Settings) {
                viewPager.setCurrentItem(1);
                return true;
            }
            return false;
        });

        serverButton.setOnClickListener((button) -> {
            Intent serviceIntent = new Intent(this, NetworkService.class);
            serviceIntent.setAction(Vals.ACTION_TOGGLE_SERVER);
            startService(serviceIntent);
        });
    }

    private void initStates() {
        Intent serviceIntent = new Intent(this, NetworkService.class);
        serviceIntent.setAction(Vals.ACTION_GET_SERVER_STATUS);
        startService(serviceIntent);
    }

    private void observeStates() {
        NetworkService.serverStatusObserver.observe(this, running -> {
            if (running) {
                ImageViewCompat.setImageTintList(serverButton, ColorStateList.valueOf(getResources().getColor(R.color.status_on)));
            } else {
                ImageViewCompat.setImageTintList(serverButton, ColorStateList.valueOf(getResources().getColor(R.color.status_off)));
            }
        });

        if (FilesCenterApp.isDebuggable())
            Data.alertNotifier.observe(this, info -> {
                errorDialogAD.setTitle(info.getString("title"));
                errorDialogAD.setMessage(info.getString("message"));
                errorDialogAD.show();
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

    public static boolean onOptionsItemSelectedStatic(Activity act , @NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.MI_Tutorial) {
            act.startActivity(new Intent(act, TutorialActivity.class));
            act.overridePendingTransition(R.anim.fragment_enter_left, R.anim.fragment_exit_left);
            return true;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, NetworkService.class);
        intent.setAction(Vals.ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN);
        startService(intent);
        super.onDestroy();
    }
}