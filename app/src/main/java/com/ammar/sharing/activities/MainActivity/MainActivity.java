package com.ammar.sharing.activities.MainActivity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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

import com.ammar.sharing.R;
import com.ammar.sharing.activities.ApksInstallerActivity.ApksInstallerActivity;
import com.ammar.sharing.activities.ChangeLogActivity.ChangeLogActivity;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.custom.ui.AdaptiveDropDown;
import com.ammar.sharing.custom.ui.AdaptiveTextView;
import com.ammar.sharing.services.ServerService;
import com.ammar.sharing.BuildConfig;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout layout;
    private ImageView themeChangeIV;
    private Toolbar toolbar;
    private View changeThemeMI;
    private View threeDotsMI;
    private View tutorialTV;
    private View apksInstallerTV;
    private FloatingActionButton serverButton;
    private ViewPager2 viewPager;
    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;
    private AlertDialog errorDialogAD;


    private SharedPreferences settingsPref;
    private SharedPreferences appInfoPref;

    public static boolean darkMode = true;
    public static boolean isFirstRun = false;
    public static Insets systemBarsPaddings;
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

    private void prepareActivity() {
        settingsPref = getSharedPreferences(Consts.PREF_SETTINGS, MODE_PRIVATE);
        darkMode = settingsPref.getBoolean(Consts.PREF_FIELD_IS_DARK, true);

        if (darkMode) {
            setTheme(R.style.AppThemeDark);
            getWindow().setBackgroundDrawableResource(R.drawable.gradient_background_dark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            setTheme(R.style.AppTheme);
            getWindow().setBackgroundDrawableResource(R.drawable.gradient_background_light);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);

        // setup language
        String lang = settingsPref.getString(Consts.PREF_FIELD_LANG, "");
        if (!lang.isEmpty()) {
            Utils.setLocale(this, lang);
        }
        // check for first Run
        appInfoPref = getSharedPreferences(Consts.PREF_APP_INFO, MODE_PRIVATE);
        isFirstRun = appInfoPref.getBoolean(Consts.PREF_FIELD_IS_FIRST_RUN, true);
        if (isFirstRun) {
            settingsPref.edit()
                    .putBoolean(Consts.PREF_FIELD_IS_DARK, true)
                    .apply();
            appInfoPref.edit().putBoolean(Consts.PREF_FIELD_IS_FIRST_RUN, false).apply();;
        }

        int lastVerCode = appInfoPref.getInt(Consts.PREF_FIELD_LAST_VERCODE, 0);
        if(BuildConfig.VERSION_CODE > lastVerCode) {
            appInfoPref.edit().putInt(Consts.PREF_FIELD_LAST_VERCODE, BuildConfig.VERSION_CODE).apply();
            startActivity(new Intent(this, ChangeLogActivity.class));
        }
    }


    private boolean warningShown = false;
    private void initItems() {
        layout = findViewById(R.id.CL_MainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(layout, (v, insets) -> {
            Insets paddings = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            MainActivity.systemBarsPaddings = paddings;
            v.setPadding(0, paddings.top, 0, 0);
            return insets;
        });

        themeChangeIV = findViewById(R.id.IV_ThemeChange);
        toolbar = findViewById(R.id.TB_Toolbar);
        changeThemeMI = findViewById(R.id.MI_ThemeToggle);
        threeDotsMI = findViewById(R.id.MI_PopupMenu);

        // setup popup window
        AdaptiveDropDown dropDown = new AdaptiveDropDown(this);
        dropDown.setAnchorView(threeDotsMI);

        tutorialTV = dropDown.addItem(R.string.tutorial, R.drawable.icon_tutorial);
        apksInstallerTV = dropDown.addItem(R.string.apks_installer, R.drawable.icon_download);

        bottomAppBar = findViewById(R.id.BAB_BottomAppBar);
        bottomNavigationView = findViewById(R.id.BottomNavView);
        serverButton = findViewById(R.id.FAB_ServerButton);
        viewPager = findViewById(R.id.MainActivityFragmentContainer);
        bottomNavigationView.setSelectedItemId(R.id.B_Share);

        MainViewPagerAdapter viewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(viewPagerAdapter);

        // this dialog shows exceptions. it's not shown when app is built for release
        errorDialogAD = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.ok, null)
                .create();


        // show warning if user didn't choose to not show it again
        final boolean isUserWantsWarning = appInfoPref.getBoolean(Consts.PREF_FIELD_IS_USER_WANTS_WARNING, true);
        if (isUserWantsWarning && !warningShown) {
            View warningDialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_warning, null, false);
            CheckBox dontShowAgainCB = warningDialogLayout.findViewById(R.id.CB_DialogWarningDontShowAgain);
            TextView dontShowAgainTV = warningDialogLayout.findViewById(R.id.TB_DialogWarningDontShowAgainTV);

            dontShowAgainTV.setOnClickListener((view) -> {
                dontShowAgainCB.toggle();
            });
            AlertDialog warningDialog = new AlertDialog.Builder(this)
                    .setView(warningDialogLayout)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener((dialog) -> {
                        boolean showAgain = !dontShowAgainCB.isChecked();
                        appInfoPref.edit().putBoolean(Consts.PREF_FIELD_IS_USER_WANTS_WARNING, showAgain).apply();
                    })
                    .create();
            // set dialog bg color
            Window window = warningDialog.getWindow();
            if (window != null)
                window.setBackgroundDrawableResource(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight);

            // show dialog
            warningDialog.show();
            warningShown = true;
        }
    }

    private void setItemsListener() {
        changeThemeMI.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
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
            Intent serviceIntent = new Intent(this, ServerService.class);
            serviceIntent.setAction(Consts.ACTION_TOGGLE_SERVER);
            startService(serviceIntent);
        });



        tutorialTV.setOnClickListener((view) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW ,Uri.parse("https://ammar64.github.io/Sharing/Tutorial"));
            if(intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.no_app_found_to_handle, Toast.LENGTH_SHORT).show();
            }
        });

        apksInstallerTV.setOnClickListener((view) -> {
            startActivity(new Intent(this, ApksInstallerActivity.class));
        });
    }


    private void initStates() {
        syncTheme(darkMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (darkMode) getWindow().setNavigationBarColor(Color.BLACK);
            else getWindow().setNavigationBarColor(Color.WHITE);
        }


        Intent serviceIntent = new Intent(this, ServerService.class);
        serviceIntent.setAction(Consts.ACTION_GET_SERVER_STATUS);
        startService(serviceIntent);

        if(Intent.ACTION_SEND.equals(getIntent().getAction()) ) {
            Intent uriIntent = new Intent(this, ServerService.class);
            uriIntent.setAction(Consts.ACTION_ADD_URI_SHARABLES);
            ArrayList<Uri> uriArrayList = new ArrayList<>(1);
            Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if( uri == null ) {
                Toast.makeText(this, R.string.unsupported_data, Toast.LENGTH_SHORT).show();
            } else {
                uriArrayList.add(uri);
                uriIntent.putParcelableArrayListExtra(Consts.EXTRA_URIS, uriArrayList);
                startService(uriIntent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
            Intent uriIntent = new Intent(this, ServerService.class);
            uriIntent.setAction(Consts.ACTION_ADD_URI_SHARABLES);
            ArrayList<Uri> uriArrayList = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if( uriArrayList == null ) {
                Toast.makeText(this, R.string.unsupported_data, Toast.LENGTH_SHORT).show();
            } else {
                uriIntent.putParcelableArrayListExtra(Consts.EXTRA_URIS, uriArrayList);
                startService(uriIntent);
            }
        }
    }

    private void observeStates() {
        Data.serverStatusObserver.observe(this, running -> {
            if (running) {
                ImageViewCompat.setImageTintList(serverButton, ColorStateList.valueOf(getResources().getColor(R.color.status_on)));
            } else {
                ImageViewCompat.setImageTintList(serverButton, ColorStateList.valueOf(getResources().getColor(R.color.status_off)));
            }
        });

        if (settingsPref.getBoolean(Consts.PREF_FIELD_DEBUG_MODE, false))
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
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                    setNavbarTheme(darkMode);
                }
            });
            anim.start();
        } else syncTheme(darkMode);
    }

    @Override
    protected void onPause() {
        settingsPref.edit().putBoolean(Consts.PREF_FIELD_IS_DARK, darkMode).apply();
        super.onPause();
    }


    public void setNavbarTheme(boolean dark) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            View view = getWindow().getDecorView();
            getWindow().setNavigationBarColor( dark ? Color.BLACK : Color.WHITE );
            view.setSystemUiVisibility( dark ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE : View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }


    public void syncTheme(boolean dark) {
        int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}};
        final int ColorPrimary = getResources().getColor(R.color.colorPrimary);

        if (dark) {
            layout.setBackgroundResource(R.drawable.gradient_background_dark);
            DrawableCompat.setTint(toolbar.getOverflowIcon(), getResources().getColor(R.color.white));
            bottomAppBar.setBackgroundTint(ColorStateList.valueOf(getResources().getColor(R.color.bottomBarColorDark)));

            ColorStateList stateList = new ColorStateList(states, new int[]{ColorPrimary, getResources().getColor(R.color.text_color_light)});
            bottomNavigationView.setItemIconTintList(stateList);
            bottomNavigationView.setItemTextColor(stateList);

            toolbar.getMenu().getItem(0).setIcon(R.drawable.icon_sun);
            toolbar.setPopupTheme(R.style.AppThemeDark);
        } else {
            layout.setBackgroundResource(R.drawable.gradient_background_light);
            DrawableCompat.setTint(toolbar.getOverflowIcon(), getResources().getColor(R.color.black));

            bottomAppBar.setBackgroundTint(ColorStateList.valueOf(getResources().getColor(R.color.bottomBarColorLight)));

            ColorStateList stateList = new ColorStateList(states, new int[]{ColorPrimary, getResources().getColor(R.color.text_color_dark)});
            bottomNavigationView.setItemIconTintList(stateList);
            bottomNavigationView.setItemTextColor(stateList);

            toolbar.getMenu().getItem(0).setIcon(R.drawable.icon_moon);

        }


        for (WeakReference<AdaptiveTextView> i : AdaptiveTextView.textViews) {
            AdaptiveTextView textViewRef = i.get();
            if (textViewRef != null) {
                textViewRef.setDark(dark);
            }
        }

        AdaptiveDropDown.setDarkAll(dark);
    }
}
