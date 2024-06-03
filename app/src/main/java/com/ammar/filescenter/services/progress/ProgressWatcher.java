package com.ammar.filescenter.services.progress;

import android.util.Log;

import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.models.Upload;

import java.util.LinkedList;
import java.util.List;

public class ProgressWatcher {
    public enum Operation {DOWNLOAD, UPLOAD}

    private Upload file;
    private int loaded = 0;



    private long requestSize = 0;


    private final Operation operation;
    private String postedFileName = null;
    public ProgressWatcher(Upload file, Operation operation) {
        this.file = file;
        this.operation = operation;
        progressSendWatchers.add(this);
        String info = "A:" + num;
        NetworkService.filesSendNotifier.postValue(info);
        num++;
    }

    public Thread executor;

    public int getPercentage() {
        switch (operation) {
            case DOWNLOAD:
                return (int) ((float) loaded / (float) file.getSize() * 100.0);
            case UPLOAD:
                return (int) ((float) loaded / (float) requestSize * 100.0);
        }
        throw new RuntimeException("OPNotSet");
    }

    public static int num = 0;
    public static List<ProgressWatcher> progressSendWatchers = new LinkedList<>();

    public void notifyEverySecond() {
        if( getSize() != 0 ) {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    do {
                        int index = progressSendWatchers.indexOf(ProgressWatcher.this);
                        String info = "P:" + index;
                        NetworkService.filesSendNotifier.postValue(info);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            remove();
                            break;
                        }
                    } while (loaded != getSize());
                    Log.d("MYLOG", "Thread stopped gracefully");
                }
            };
            executor = new Thread(runnable);
            executor.start();
        }
    }


    public void accumulate(int bytes) {
        loaded += bytes;
    }

    private boolean removed = false;

    public void remove() {
        if (!removed) {
            int index = progressSendWatchers.indexOf(this);
            progressSendWatchers.remove(this);
            NetworkService.filesSendNotifier.postValue("R:" + index);
            removed = true;
        }
    }
    public String getFileName() {
        switch (operation) {
            case DOWNLOAD:
                return file.getFileName();
            case UPLOAD:
                return postedFileName;
        }
        throw new RuntimeException("NoOPSet");
    }
    public long getSize() {
        switch (operation) {
            case DOWNLOAD:
                return file.getSize();
            case UPLOAD:
                return requestSize;
        }
        throw new RuntimeException("OPNotSet");
    }

    public Operation getOperation() {
        return operation;
    }

    public void setRequestInfo( String filename ,long requestSize) {
        this.postedFileName = filename;
        this.requestSize = requestSize;
    }

}
