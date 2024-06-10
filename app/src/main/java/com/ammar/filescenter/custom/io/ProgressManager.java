package com.ammar.filescenter.custom.io;

import android.os.Bundle;

import com.ammar.filescenter.services.NetworkService;

import java.net.SocketAddress;
import java.util.LinkedList;

public class ProgressManager {


    private String fileName;

    // The other device receiving or sending
    private String remoteIp;



    public enum OP {DOWNLOAD, UPLOAD}

    private final OP op;

    // if loaded is -1 it's completed.
    private long loaded;
    // total file size
    private long total;


    private int index;

    private void setIndex(int index) {
        this.index = index;
        this.progress_info.putInt("index", index);
    }

    public static LinkedList<ProgressManager> progresses = new LinkedList<>();

    public static void removeProgress(int index) {
        for (int i = index + 1; i < progresses.size(); i++) {
            progresses.get(i).setIndex(progresses.get(i).index - 1);
        }
        progresses.remove(index);
    }


    public ProgressManager(String fileName, long fileSize, SocketAddress remoteIpAddress, OP opType) {
        this.fileName = fileName;
        this.total = fileSize;
        this.remoteIp = remoteIpAddress.toString();
        this.remoteIp = this.remoteIp.substring(1, this.remoteIp.lastIndexOf(":"));
        this.op = opType;
        progresses.add(this);
        index = progresses.lastIndexOf(this);

        // notify the UI item is added


        Bundle info_add = new Bundle();
        info_add.putChar("action", 'A');
        info_add.putInt("index", index);
        NetworkService.filesSendNotifier.forcePostValue(info_add);

        // set action to P for later use
        progress_info.putChar("action", 'P');
        progress_info.putInt("index", index);
    }

    public long getTotal() {
        return total;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public long getLoaded() {
        return loaded;
    }


    long bytesPerSecond = 0;

    public long getSpeed() {
        return bytesPerSecond;
    }

    public OP getOperation() {
        return op;
    }

    long previouslyLoaded = 0;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLoaded(long n) {
        loaded = n;
        reportProgress();
    }

    public void setTotal(long n) {
        total = n;
    }

    public int getPercentage() {
        if (loaded < 0) return 100;
        return (int) ((float) loaded / (float) total * 100.0f);
    }

    public void setAsCompleted() {
        loaded = -1;
    }


    private long lastTime = 0;

    private final Bundle progress_info = new Bundle();


    public void reportProgress() {
        if ( System.currentTimeMillis() - lastTime >= 300) {
            NetworkService.filesSendNotifier.postValue(progress_info);
            bytesPerSecond = getLoaded() - previouslyLoaded;
            previouslyLoaded = getLoaded();
            lastTime = System.currentTimeMillis();
        }
    }


    public static final int COMPLETED = -1;
    public static final int FAILED    = -2;


    public void reportCompleted() {
        setLoaded(COMPLETED);
        NetworkService.filesSendNotifier.postValue(progress_info);
    }

    public void reportFailed() {
        setLoaded(FAILED);
        NetworkService.filesSendNotifier.postValue(progress_info);
    }
}
