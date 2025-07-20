package com.ammar.sharing.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Build;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.R;
import com.ammar.sharing.network.Server;
import com.ammar.sharing.network.utils.WebAppUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Locale;


public class SharingApp extends Application {
    private static boolean _isDebuggable;

    @Override
    public void onCreate() {
        super.onCreate();
        _isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        Utils.setupUtils(this);
        WebAppUtils.init();
        Consts.systemLocale = Locale.getDefault();
        Consts.langCodes = getResources().getStringArray(R.array.lang_codes);


        // Check if Android version is Oreo or higher, as notification channels are required from Oreo onwards
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importanceHigh = NotificationManager.IMPORTANCE_HIGH;

            // Server notification channel.
            // NOTE: This only posts one notification the one that tells you the server is running
            CharSequence serverChannelName = "Server";
            NotificationChannel serverChannel = new NotificationChannel(Consts.SERVER_NOTIFICATION_CHANNEL_ID, serverChannelName, importanceHigh);
            serverChannel.setDescription("Informs you when the server is running");
            serverChannel.enableLights(true);
            serverChannel.setLightColor(Color.GREEN);

            CharSequence progressChannelName = "Progress channel";
            NotificationChannel progressChannel = new NotificationChannel(Consts.PROGRESS_NOTIFICATION_CHANNEL_ID, progressChannelName, importanceHigh);
            progressChannel.setDescription("Shows the progress of file transfers");
            progressChannel.enableLights(true);
            progressChannel.setLightColor(Color.BLUE);

            notificationManager.createNotificationChannels(Arrays.asList(serverChannel, progressChannel));
        }

        SharedPreferences settingsPref = getSharedPreferences(Consts.PREF_SETTINGS, MODE_PRIVATE);
        Server.PORT_NUMBER = settingsPref.getInt(Consts.PREF_FIELD_SERVER_PORT, 2999);
    }

    public static boolean isDebuggable() {
        return _isDebuggable;
    }
}
