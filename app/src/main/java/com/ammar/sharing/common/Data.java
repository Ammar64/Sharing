package com.ammar.sharing.common;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.ammar.sharing.custom.data.QueueMutableLiveData;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class Data {
    // observers
    public static final MutableLiveData<Boolean> serverStatusObserver = new MutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> downloadsListNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> filesSendNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Bundle> usersListObserver = new QueueMutableLiveData<>();
    public static final MutableLiveData<Bundle> alertNotifier = new MutableLiveData<>();
    public static final QueueMutableLiveData<Integer> messagesNotifier = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<IceCandidate> webRTCIceCandidate = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<SessionDescription> webRTCSessionDescription = new QueueMutableLiveData<>();
    public static final QueueMutableLiveData<Void> webRTCStartOffer = new QueueMutableLiveData<>();


    private Data(){}
}
