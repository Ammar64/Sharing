package com.ammar.filescenter.services.objects;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.util.UUID;

public class AppDownloadable extends Downloadable {
    public AppDownloadable(Context context, String package_id) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = pm.getApplicationInfo(package_id, 0);
        String apkPath = appInfo.sourceDir;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (appInfo.splitSourceDirs != null) {
                splits = new Downloadable[appInfo.splitSourceDirs.length];
                for (int i = 0; i < appInfo.splitSourceDirs.length; i++) {
                    splits[i] = new Downloadable(appInfo.splitSourceDirs[i]);
                }
                hasSplits = true;
            } else {
                hasSplits = false;
            }
        }


        this.uuid = UUID.randomUUID();
        this.path = apkPath;
        this.size = new File(path).length();
    }

    boolean hasSplits;
    public Downloadable[] splits;
    public String appName;
}
