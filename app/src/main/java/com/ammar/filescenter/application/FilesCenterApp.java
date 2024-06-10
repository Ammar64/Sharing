package com.ammar.filescenter.application;

import android.app.Application;
import android.os.Build;

import com.ammar.filescenter.utils.Utils;


public class FilesCenterApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Utils.setRes(
                getResources()
        );
    }
}
