package com.ammar.sharing.common;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.ammar.sharing.custom.data.QueueMutableLiveData;
import com.ammar.sharing.models.User;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;


public class LiveDataSingletons {
    // observers
    public static final MutableLiveData<Boolean> serverStatusObserver = new MutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> downloadsListNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> filesSendNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> usersListObserver = new QueueMutableLiveData<>();
    public static final MutableLiveData<Bundle> alertNotifier = new MutableLiveData<>();
    public static final QueueMutableLiveData<Integer> messagesNotifier = new QueueMutableLiveData<>();

    private LiveDataSingletons(){}
}
