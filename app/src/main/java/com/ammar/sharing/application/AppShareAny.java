package com.ammar.sharing.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.ammar.sharing.BuildConfig;
import com.ammar.sharing.common.Global;
import com.ammar.sharing.common.utils.FileUtils;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.R;
import com.ammar.sharing.network.WebServer;
import com.ammar.sharing.network.WebAssetsUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;


public class AppShareAny extends Application {
    static {
        System.loadLibrary("nativeutils");
        System.loadLibrary("backend_adapter");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FileUtils.init(this);

        Utils.setupUtils(this);
        setupPrefs();
        File webAssetsPath = new File(getFilesDir(), "web_assets");
        Global.WEB_ASSETS_PATH = webAssetsPath;

        if(Global.IS_FIRST_RUN_AFTER_UPDATE || BuildConfig.DEBUG) {
            try {
                FileUtils.deleteDirRecursively(webAssetsPath);
            } catch (IOException e) {
                Log.e(Global.TAG, "IOException in AppShareAny when attempting to delete old web assets after update. Error: " + e);
            }
        }
        if (!webAssetsPath.exists()) {
            webAssetsPath.mkdir();
            WebAssetsUtils.init(getAssets());
        }
        SharedPreferences settingsPref = getSharedPreferences(Global.PREF_SETTINGS, MODE_PRIVATE);
        WebServer.PORT_NUMBER = settingsPref.getInt(Global.PREF_FIELD_SERVER_PORT, 2999);
        WebServer.IS_HTTPS = settingsPref.getBoolean(Global.PREF_FIELD_IS_HTTPS, true);


        Global.systemLocale = Locale.getDefault();
        Global.langCodes = getResources().getStringArray(R.array.lang_codes);


        // Check if Android version is Oreo or higher, as notification channels are required from Oreo onwards
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importanceHigh = NotificationManager.IMPORTANCE_HIGH;

            // Server notification channel.
            // NOTE: This only posts one notification the one that tells you the server is running
            CharSequence serverChannelName = "Server";
            NotificationChannel serverChannel = new NotificationChannel(Global.SERVER_NOTIFICATION_CHANNEL_ID, serverChannelName, importanceHigh);
            serverChannel.setDescription("Informs you when the server is running");
            serverChannel.enableLights(true);
            serverChannel.setLightColor(Color.GREEN);

            CharSequence progressChannelName = "Progress channel";
            NotificationChannel progressChannel = new NotificationChannel(Global.PROGRESS_NOTIFICATION_CHANNEL_ID, progressChannelName, importanceHigh);
            progressChannel.setDescription("Shows the progress of file transfers");
            progressChannel.enableLights(true);
            progressChannel.setLightColor(Color.BLUE);

            notificationManager.createNotificationChannels(Arrays.asList(serverChannel, progressChannel));
        }
    }

    private void setupPrefs() {
        // check for first Run
        SharedPreferences appInfoPref = getSharedPreferences(Global.PREF_APP_INFO, MODE_PRIVATE);
        Global.IS_FIRST_RUN = appInfoPref.getBoolean(Global.PREF_FIELD_IS_FIRST_RUN, true);

        if (Global.IS_FIRST_RUN) {
            Utils.getSettings().edit()
                    .putBoolean(Global.PREF_FIELD_IS_DARK, true)
                    .apply();
            appInfoPref.edit().putBoolean(Global.PREF_FIELD_IS_FIRST_RUN, false).apply();
        }

        int lastVerCode = appInfoPref.getInt(Global.PREF_FIELD_LAST_VERCODE, 0);
        if (BuildConfig.VERSION_CODE > lastVerCode) {
            Global.IS_FIRST_RUN_AFTER_UPDATE = true;
            appInfoPref.edit().putInt(Global.PREF_FIELD_LAST_VERCODE, BuildConfig.VERSION_CODE).apply();
        } else {
            Global.IS_FIRST_RUN_AFTER_UPDATE = false;
        }

        // setup language
        String lang = Utils.getSettings().getString(Global.PREF_FIELD_LANG, "");
        if (!lang.isEmpty()) {
            Utils.setLocale(this, lang);
        }
    }

    @SuppressWarnings("JavaJniMissingFunction")
    private native void initJavaAssetsManager();
}
