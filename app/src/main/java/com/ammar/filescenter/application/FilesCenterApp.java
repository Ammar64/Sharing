package com.ammar.filescenter.application;

import android.app.Application;

import com.ammar.filescenter.common.Utils;


public class FilesCenterApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Utils.setRes(
                getResources()
        );
    }
}
