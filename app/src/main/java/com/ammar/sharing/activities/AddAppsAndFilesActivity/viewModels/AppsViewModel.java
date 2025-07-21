package com.ammar.sharing.activities.AddAppsAndFilesActivity.viewModels;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.AppData;
import com.ammar.sharing.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AppsViewModel extends ViewModel {
    private static final MutableLiveData<AppData[]> mListedApps = new MutableLiveData<>();
    private static final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>(true);
    private static boolean dataLoaded = false;

    public LiveData<AppData[]> getAppsLiveData() {
        return mListedApps;
    }
    public LiveData<Boolean> getIsLoadingLiveData() {
        return mIsLoading;
    }

    public AppsViewModel() {
        loadData();
    }

    public void loadData() {
        if (!dataLoaded) {
            loadAppsData();
            dataLoaded = true;
        }
    }

    public void reset() {
        mIsLoading.setValue(true);
        dataLoaded = false;
    }

    private void loadAppsData() {
        new Thread(() -> {
            ArrayList<ApplicationInfo> userApps = new ArrayList<>(30);
            int flags = PackageManager.GET_META_DATA |
                    PackageManager.GET_SHARED_LIBRARY_FILES |
                    PackageManager.GET_UNINSTALLED_PACKAGES;

            PackageManager pm = Utils.getPm();
            List<ApplicationInfo> applications = pm.getInstalledApplications(flags);
            for (ApplicationInfo appInfo : applications) {
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                    userApps.add(appInfo);
                }
            }
            AppData[] appsData = new AppData[userApps.size()];

            for (int i = 0; i < userApps.size(); i++) {
                String packageName = userApps.get(i).packageName;
                boolean hasSplits = userApps.get(i).splitPublicSourceDirs != null;
                String appLabel = pm.getApplicationLabel(userApps.get(i)).toString();
                Drawable appIcon = userApps.get(i).loadIcon(pm);
                // add app data
                appsData[i] = new AppData(packageName, hasSplits, appLabel, appIcon);
            }

            mIsLoading.postValue(false);
            mListedApps.postValue(appsData);
        }).start();
    }

}
