package com.ammar.filescenter.custom.data;

import androidx.annotation.MainThread;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;
import java.util.Queue;

public class QueueMutableLiveData<T> extends MutableLiveData<T> {
    private final Queue<T> queuedValues = new LinkedList<T>();
    private boolean isValueForced = false;
    public synchronized void forcePostValue(T value) {

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
