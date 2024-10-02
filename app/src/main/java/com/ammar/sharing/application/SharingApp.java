package com.ammar.sharing.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Build;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Utils;

import java.util.Locale;


public class SharingApp extends Application {
    private static boolean _isDebuggable;

    @Override
    public void onCreate() {
        super.onCreate();
        _isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
        Utils.setupUtils(this);
        Consts.SystemLocale = Locale.getDefault();


        // make notification channel
        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channel name
        CharSequence channelName = "Server";
        // Notification importance level

        // Check if Android version is Oreo or higher, as notification channels are required from Oreo onwards
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            // Create a notification channel
            NotificationChannel channel = new NotificationChannel(Consts.serverNotificationChannelID, channelName, importance);
            // Configure the notification channel
            channel.setDescription("Informs you when the server is running");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            // Register the notification channel with the system
            notificationManager.createNotificationChannel(channel);
        }
    }
    public static boolean isDebuggable() { return _isDebuggable; }
}
