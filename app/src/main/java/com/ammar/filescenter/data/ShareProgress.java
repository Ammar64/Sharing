package com.ammar.filescenter.data;

import android.os.Bundle;

import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

// I use this for data I want to make sure to arrive
// QueuedMutableLiveData is not that good
public class ShareProgress {
    private static final Subject<Bundle> shareProgressSubject = PublishSubject.create();
    public static void updateShareProgressSubject(Bundle bundle) {
        shareProgressSubject.onNext(bundle);
    }

    public static Subject<Bundle> getShareProgressSubject() {
        return shareProgressSubject;
    }
    private ShareProgress(){}
}
