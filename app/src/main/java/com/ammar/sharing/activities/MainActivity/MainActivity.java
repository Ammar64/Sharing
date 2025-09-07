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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.viewpager2.widget.ViewPager2;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.ApksInstallerActivity.ApksInstallerActivity;
import com.ammar.sharing.activities.ChangeLogActivity.ChangeLogActivity;
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders.HeaderViewHolder;
import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.MessagesAdapter;
import com.ammar.sharing.activities.SettingsActivity.SettingActivityResultsContract;
import com.ammar.sharing.activities.SettingsActivity.SettingsActivity;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.common.utils.UsersNotifier;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.ui.AdaptiveDropDown;
import com.ammar.sharing.custom.ui.AdaptiveTextView;
import com.ammar.sharing.models.Message;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.sessions.MessagesWSSession;
import com.ammar.sharing.services.ServerService;
import com.ammar.sharing.BuildConfig;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private View settingsTV;
    private View apksInstallerTV;
    private ViewPager2 viewPager;
    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;
    private AlertDialog errorDialogAD;


    private SharedPreferences settingsPref;
    private SharedPreferences appInfoPref;

    public static boolean sDarkMode = true;
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
        sDarkMode = settingsPref.getBoolean(Consts.PREF_FIELD_IS_DARK, true);

        if (sDarkMode) {
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
            appInfoPref.edit().putBoolean(Consts.PREF_FIELD_IS_FIRST_RUN, false).apply();
        }

        int lastVerCode = appInfoPref.getInt(Consts.PREF_FIELD_LAST_VERCODE, 0);
        if (BuildConfig.VERSION_CODE > lastVerCode) {
            appInfoPref.edit().putInt(Consts.PREF_FIELD_LAST_VERCODE, BuildConfig.VERSION_CODE).apply();
            startActivity(new Intent(this, ChangeLogActivity.class));
        }
    }


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
        settingsTV = dropDown.addItem(R.string.settings, R.drawable.icon_settings);
        apksInstallerTV = dropDown.addItem(R.string.apks_installer, R.drawable.icon_download);

        bottomAppBar = findViewById(R.id.BAB_BottomAppBar);
        bottomNavigationView = findViewById(R.id.BottomNavView);
        viewPager = findViewById(R.id.MainActivityFragmentContainer);
        bottomNavigationView.setSelectedItemId(R.id.B_AppShare);

        MainViewPagerAdapter viewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(viewPagerAdapter);

        // this dialog shows exceptions. it's not shown when app is built for release
        errorDialogAD = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.ok, null)
                .create();
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
                Log.d("page", "onPageSelected: " + position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                // app share is 0, browser share is 1
                // set title
                switch (position) {
                    case 0:
                        toolbar.setTitle(R.string.app_name);
                        break;
                    case 1:
                        toolbar.setTitle(R.string.share);
                        break;
                }
            }


        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.B_AppShare) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (id == R.id.B_BrowserShare) {
                viewPager.setCurrentItem(1);
                return true;
            }
            return false;
        });

        tutorialTV.setOnClickListener((view) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ammar64.github.io/Sharing/Tutorial"));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.no_app_found_to_handle, Toast.LENGTH_SHORT).show();
            }
        });

        settingsTV.setOnClickListener((view) -> {
            settingsActivityLauncher.launch(null);
        });

        apksInstallerTV.setOnClickListener((view) -> {
            startActivity(new Intent(this, ApksInstallerActivity.class));
        });
    }


    private void initStates() {
        syncTheme(sDarkMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sDarkMode) getWindow().setNavigationBarColor(Color.BLACK);
            else getWindow().setNavigationBarColor(Color.WHITE);
        }


        Intent serviceIntent = new Intent(this, ServerService.class);
        serviceIntent.setAction(ServerService.ACTION_GET_SERVER_STATUS);
        startService(serviceIntent);

        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            if ("text/plain".equals(getIntent().getType())) {
                String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                Message message = new Message(text);
                synchronized (MessagesAdapter.messages) {
                    MessagesAdapter.messages.add(message);
                    // notify UI that a message was received
                    HeaderViewHolder.unseenMessagesCount++;
                    Data.messagesNotifier.forcePostValue(MessagesAdapter.messages.size());
                    for (User i : User.users) {
                        if (i.isWebSocketConnected(MessagesWSSession.path)) {
                            i.getWebSocket(MessagesWSSession.path).sendText(message.toJSON().toString());
                        }
                    }
                }
            } else {
                Intent uriIntent = new Intent(this, ServerService.class);
                uriIntent.setAction(ServerService.ACTION_ADD_URIS);
                ArrayList<Uri> uriArrayList = new ArrayList<>(1);
                Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri == null) {
                    Toast.makeText(this, R.string.unsupported_data, Toast.LENGTH_SHORT).show();
                } else {
                    uriArrayList.add(uri);
                    uriIntent.putParcelableArrayListExtra(ServerService.EXTRA_URIS, uriArrayList);
                    startService(uriIntent);
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
            Intent uriIntent = new Intent(this, ServerService.class);
            uriIntent.setAction(ServerService.ACTION_ADD_URIS);
            ArrayList<Uri> uriArrayList = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uriArrayList == null) {
                Toast.makeText(this, R.string.unsupported_data, Toast.LENGTH_SHORT).show();
            } else {
                uriIntent.putParcelableArrayListExtra(ServerService.EXTRA_URIS, uriArrayList);
                startService(uriIntent);
            }
        }
    }

    private void observeStates() {
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
        sDarkMode = !sDarkMode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (themeChangeIV.getVisibility() == View.VISIBLE) {
                return;
            }

            int w = layout.getMeasuredWidth();
            int h = layout.getMeasuredHeight();

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            layout.draw(canvas);

            syncTheme(sDarkMode);

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
                    setNavbarTheme(sDarkMode);
                }
            });
            anim.start();
        } else syncTheme(sDarkMode);

        // notify browsers
        UsersNotifier.notifyUsersOfUIChange();
    }

    @Override
    protected void onPause() {
        settingsPref.edit().putBoolean(Consts.PREF_FIELD_IS_DARK, sDarkMode).apply();
        super.onPause();
    }


    public void setNavbarTheme(boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View view = getWindow().getDecorView();
            getWindow().setNavigationBarColor(dark ? Color.BLACK : Color.WHITE);
            view.setSystemUiVisibility(dark ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE : View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
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

    ActivityResultLauncher<Void> settingsActivityLauncher = registerForActivityResult(
            new SettingActivityResultsContract(),
            (shouldRecreate) -> {
                if (shouldRecreate) {
                    recreate();
                }
            }
    );
}
