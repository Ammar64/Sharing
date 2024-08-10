package com.ammar.sharing.application;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.network.sessions.Sessions;

import java.util.Locale;


public class SharingApp extends Application {
    private static boolean _isDebuggable;

    @Override
    public void onCreate() {
        super.onCreate();
        _isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
        Utils.setupUtils(this);
        Sessions.defineSessions();
        Consts.SystemLocale = Locale.getDefault();
    }
    public static boolean isDebuggable() { return _isDebuggable; }
}
