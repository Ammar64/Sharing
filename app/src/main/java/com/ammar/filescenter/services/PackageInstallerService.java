package com.ammar.filescenter.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class PackageInstallerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // TODO: Learn how to install multi apk packages
    // The reason we need to implement a package installer is so user can
    // send this app to the other device through the app itself and then
    // use the installer to install multi apk packages.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }
}
