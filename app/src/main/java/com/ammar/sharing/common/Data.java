package com.ammar.sharing.common;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.ammar.sharing.custom.data.QueueMutableLiveData;
import com.ammar.sharing.models.Message;

public class Data {
    // observers
    public static final MutableLiveData<Boolean> serverStatusObserver = new MutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> filesListNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> filesSendNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> usersListObserver = new QueueMutableLiveData<>();
    public static final MutableLiveData<Bundle> alertNotifier = new MutableLiveData<>();
    public static final QueueMutableLiveData<Integer> messagesNotifier = new QueueMutableLiveData<>();

    private Data(){}
}
