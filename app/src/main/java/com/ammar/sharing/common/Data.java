package com.ammar.filescenter.common;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

public class Data {
    public static MutableLiveData<Bundle> alertNotifier = new MutableLiveData<>();

    private Data(){}
}
