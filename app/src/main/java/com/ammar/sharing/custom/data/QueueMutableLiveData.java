package com.ammar.sharing.custom.data;

import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.MainThread;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;
import java.util.Queue;

@Keep
public class QueueMutableLiveData<T> extends MutableLiveData<T> {
    private final Queue<T> queuedValues = new LinkedList<T>();
    private boolean isValueForced = false;
    public synchronized void forcePostValue(T value) {
        Log.d("RUST_JAVA", "forcePostValue called");
        queuedValues.offer(value);
        super.postValue(value);
        isValueForced = true;
    }

    @Override
    @MainThread
    public synchronized void setValue(T value) {
        if(isValueForced) {
            isValueForced = false;
            queuedValues.remove(value);

            queuedValues.offer(value);
            while (!queuedValues.isEmpty())
                super.setValue(queuedValues.poll());
        } else {
            super.setValue(value);
        }
    }
}
