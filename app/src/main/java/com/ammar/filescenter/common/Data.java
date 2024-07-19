package com.ammar.filescenter.common;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.ammar.filescenter.custom.data.QueueMutableLiveData;

public class Data {
    // observers
    public static final MutableLiveData<Boolean> serverStatusObserver = new MutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> filesListNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> filesSendNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> usersListObserver = new QueueMutableLiveData<>();
    public static final MutableLiveData<Bundle> alertNotifier = new MutableLiveData<>();
    private Data(){}
}
