package com.ammar.filescenter.services.progress;

import android.os.Handler;
import android.util.Log;

import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.models.Upload;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressSendWatcher {

    private Upload file;
    private int loaded = 0;

    public ProgressSendWatcher(Upload file) {
        this.file = file;
        progressSendWatchers.add(this);
        String info = "A:" + num;
        NetworkService.filesSendNotifier.postValue(info);
        num++;
    }

    public Thread executor;

    public int getPercentage() {
        return (int) ((float) loaded / (float) file.getSize() * 100.0);
    }

    public static int num = 0;
    public static List<ProgressSendWatcher> progressSendWatchers = new LinkedList<>();

    public void notifyEverySecond() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                do {
                    int index = progressSendWatchers.indexOf(ProgressSendWatcher.this);
                    String info = "P:" + index + ":" + loaded;
                    NetworkService.filesSendNotifier.postValue(info);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        remove();
                        break;
                    }
                } while (loaded != file.getSize());
                Log.d("MYLOG", "Thread stopped gracefully");
            }
        };
        executor = new Thread(runnable);
        executor.start();
    }


    public void accumulate(int bytes) {
        loaded += bytes;
    }

    private boolean removed = false;
    public void remove() {
        if( !removed ) {
            int index = progressSendWatchers.indexOf(this);
            progressSendWatchers.remove(this);
            NetworkService.filesSendNotifier.postValue("R:" + index);
            removed = true;
        }
    }

    public Upload getFile() {
        return file;
    }
}
