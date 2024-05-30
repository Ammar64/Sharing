package com.ammar.filescenter.services.objects;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.util.UUID;

import com.ammar.filescenter.services.components.Server;

public class AppDownloadable extends Downloadable {
    public AppDownloadable(Context context, String package_id) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(package_id, 0);
            String apkPath = appInfo.sourceDir;
            Log.d("MYLOG", "ApkPath: " + apkPath);

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
            } else hasSplits = false;

        this.appName = appInfo.loadLabel(pm).toString();
        this.uuid = UUID.randomUUID();
        this.path = apkPath;
        this.size = new File(path).length();

        } catch (PackageManager.NameNotFoundException ignore) {

        }
    }

    @Override
    public String getName() {
        return appName + ".apk";
    }

    public boolean isWithSplits() {
        return hasSplits;
    }
    private boolean hasSplits;
    public Downloadable[] splits;
    public String appName;

}
