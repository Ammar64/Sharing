package com.ammar.filescenter.custom.data;

import androidx.annotation.MainThread;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;
import java.util.Queue;

public class QueueMutableLiveData<T> extends MutableLiveData<T> {
    private final Queue<T> queuedValues = new LinkedList<T>();

    @Override
    public synchronized void postValue(T value) {

        queuedValues.offer(value);
        super.postValue(value);
    }

    @Override
    @MainThread
    public synchronized void setValue(T value) {
        queuedValues.remove(value);

        queuedValues.offer(value);
        while(!queuedValues.isEmpty())
            super.setValue(queuedValues.poll());
    }
}
