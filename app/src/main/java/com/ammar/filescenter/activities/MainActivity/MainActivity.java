package com.ammar.filescenter.activities.MainActivity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.fragments.SettingsFragment;
import com.ammar.filescenter.activities.TutorialActivity.TutorialActivity;
import com.ammar.filescenter.application.FilesCenterApp;
import com.ammar.filescenter.common.Data;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.common.Vals;
import com.ammar.filescenter.custom.ui.TrackedTextView;
import com.ammar.filescenter.services.NetworkService;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FrameLayout drawerContainer;
    private CoordinatorLayout layout;
    private ImageView themeChangeIV;
    private Toolbar toolbar;
    private View changeThemeMI;
    private FloatingActionButton serverButton;
    private ViewPager2 viewPager;
    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;
    private AlertDialog errorDialogAD;


    private SharedPreferences settingsPref;
    public static boolean darkMode = false;
    public final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    public final int REQUEST_CODE_NOTIFICATION_PERMISSION = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        prepareActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initItems();
        requestPermissions();
        setItemsListener();
        initStates();
        observeStates();
    }

    public void prepareActivity() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);


        settingsPref = getSharedPreferences(SettingsFragment.SettingsPrefFile, MODE_PRIVATE);
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
//        String lang = settingsPref.getString(SettingsFragment.Language, "");
//        if (!lang.isEmpty()) {
//            Utils.setLocale(this, lang);
//        }

    }

    private void initItems() {
        layout = findViewById(R.id.CL_MainLayout);
        drawerContainer = findViewById(R.id.FL_drawerContainerLayout);

        ViewCompat.setOnApplyWindowInsetsListener(layout, (v, insets) -> {
            Insets paddings = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, paddings.top, 0, 0);
            return insets;
        });
        themeChangeIV = findViewById(R.id.IV_ThemeChange);
        toolbar = findViewById(R.id.TB_Toolbar);
        changeThemeMI = findViewById(R.id.MI_ThemeToggle);

        bottomAppBar = findViewById(R.id.BAB_BottomAppBar);
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
        toolbar.setOnMenuItemClickListener((item) -> {
            int id = item.getItemId();
            if (id == R.id.MI_ThemeToggle) {
                return true;
            } else if (id == R.id.MI_Tutorial) {
                startActivity(new Intent(this, TutorialActivity.class));
                overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
                return true;
            }
            return false;
        });

        changeThemeMI.setOnTouchListener((view, event) -> {
            if( event.getAction() == MotionEvent.ACTION_UP ) {
                view.performClick();
                int[] pos = new int[2];
                pos[0] = (int) event.getRawX();
                pos[1] = (int) event.getRawY();

                changeTheme(pos);
            }
            return true;
        });

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

                // share is 0, settings is 2, 1 is nothing
                // set title
                switch (position) {
                    case 0:
                        toolbar.setTitle(R.string.share);
                        break;
                    case 2:
                        toolbar.setTitle(R.string.settings);
                        break;
                }
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
        syncTheme(darkMode);
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

    private void changeTheme(int[] pos) {
        darkMode = !darkMode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (themeChangeIV.getVisibility() == View.VISIBLE) {
                return;
            }

            int w = layout.getMeasuredWidth();
            int h = layout.getMeasuredHeight();

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            layout.draw(canvas);

            syncTheme(darkMode);

            themeChangeIV.setImageBitmap(bitmap);
            themeChangeIV.setVisibility(View.VISIBLE);

            float finalRadius = (float) Math.max(Math.sqrt((w - pos[0]) * (w - pos[0]) + (h - pos[1]) * (h - pos[1])), Math.sqrt(pos[0] * pos[0] + (h - pos[1]) * (h - pos[1])));
            Log.d("MYLOG", String.format(Locale.ENGLISH, "Location: (%d, %d)", pos[0], pos[1]));
            Animator anim = ViewAnimationUtils.createCircularReveal(layout, pos[0], pos[1], 0, finalRadius);
            anim.setDuration(400);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    themeChangeIV.setImageDrawable(null);
                    themeChangeIV.setVisibility(View.GONE);
                }
            });
            anim.start();
        } else syncTheme(darkMode);
    }

    @Override
    protected void onDestroy() {
        settingsPref.edit().putBoolean(SettingsFragment.DarkModeKey, darkMode).apply();
//        Intent intent = new Intent(this, NetworkService.class);
//        intent.setAction(Vals.ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN);
//        startService(intent);
        super.onDestroy();
    }

    public void syncTheme(boolean dark) {
        int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}};
        final int ColorPrimary = getResources().getColor(R.color.colorPrimary);
        int textsColor;
        if (dark) {
            layout.setBackgroundResource(R.drawable.gradient_background_dark);
            DrawableCompat.setTint(toolbar.getOverflowIcon(), getResources().getColor(R.color.white));
            bottomAppBar.setBackgroundTint(ColorStateList.valueOf(getResources().getColor(R.color.bottomBarColorDark)));

            ColorStateList stateList = new ColorStateList(states, new int[]{ColorPrimary, getResources().getColor(R.color.text_color_light)});
            bottomNavigationView.setItemIconTintList(stateList);
            bottomNavigationView.setItemTextColor(stateList);

            textsColor = getResources().getColor(R.color.text_color_light);
        } else {
            layout.setBackgroundResource(R.drawable.gradient_background_light);
            DrawableCompat.setTint(toolbar.getOverflowIcon(), getResources().getColor(R.color.black));
            bottomAppBar.setBackgroundTint(ColorStateList.valueOf(getResources().getColor(R.color.bottomBarColorLight)));

            ColorStateList stateList = new ColorStateList(states, new int[]{ColorPrimary, getResources().getColor(R.color.text_color_dark)});
            bottomNavigationView.setItemIconTintList(stateList);
            bottomNavigationView.setItemTextColor(stateList);

            textsColor = getResources().getColor(R.color.text_color_dark);
        }

        for (WeakReference<TrackedTextView> i : TrackedTextView.textViews) {
            TrackedTextView textViewRef = i.get();
            if( textViewRef != null ) {
                textViewRef.setTextColor(textsColor);
            }
        }
    }
}