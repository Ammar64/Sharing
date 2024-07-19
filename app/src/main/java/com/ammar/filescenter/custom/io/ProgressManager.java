package com.ammar.filescenter.custom.io;

import android.os.Bundle;

import com.ammar.filescenter.common.Data;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.models.User;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ProgressManager {

    private final File file;
    private Socket socket;
    private String displayName = null;
    // The other device receiving or sending
    private final User user;

    public enum OP {DOWNLOAD, UPLOAD}

    private final OP op;

    // if loaded is -1 it's completed.
    private long loaded;
    // total file size
    private long total;
    private int index;
    private String uuid = null;

    private void setIndex(int index) {
        this.index = index;
        this.progress_info.putInt("index", index);
    }

    public int getIndex() { return index; }

    public static ArrayList<ProgressManager> progresses = new ArrayList<>();

    public static void removeProgress(int index) {
        for (int i = index + 1; i < progresses.size(); i++) {
            progresses.get(i).setIndex(progresses.get(i).index - 1);
        }
        progresses.remove(index);

        Bundle b = new Bundle();
        b.putChar("action", 'R');
        b.putInt("index", index);
        Data.filesSendNotifier.forcePostValue(b);
    }


    public ProgressManager(File file, Socket socket, long total, User user, OP opType) {
        this.file = file;
        this.socket = socket;
        this.total = total;
        this.user = user;
        this.op = opType;
        progresses.add(this);
        index = progresses.lastIndexOf(this);

        // notify the UI item is added


        Bundle info_add = new Bundle();
        info_add.putChar("action", 'A');
        info_add.putInt("index", index);
        Data.filesSendNotifier.forcePostValue(info_add);
        // set action to P for later use
        progress_info.putChar("action", 'P');
        progress_info.putInt("index", index);
    }


    public long getTotal() {
        return total;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getDisplayName() {
        return displayName == null ? file.getName() : displayName;
    }

    public File getFile() {
        return file;
    }

    public String getFileType() {
        return Utils.getMimeType(file.getName());
    }

    public User getUser() {
        return user;
    }

    public long getLoaded() {
        return loaded;
    }

    public String getUUID() {
        return uuid;
    }

    long transferSpeed = 0;

    public long getSpeed() {
        return (long) (transferSpeed * (1000.0f / 300.0f));
    }

    public OP getOperation() {
        return op;
    }

    long previouslyLoaded = 0;

    public void setLoaded(long n) {
        loaded = n;
        reportProgress();
    }

    public void accumulateLoaded(int bytesRead) {
        if (loaded >= 0)
            loaded += bytesRead;
        reportProgress();
    }

    public void setTotal(long n) {
        total = n;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    public int getPercentage() {
        if (loaded < 0) return 100;
        return (int) ((float) loaded / (float) total * 100.0f);
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    private long lastTime = 0;

    private final Bundle progress_info = new Bundle();


    public void reportProgress() {
        if (System.currentTimeMillis() - lastTime >= 300) {
            Data.filesSendNotifier.postValue(progress_info); // notify the UI of changes
            transferSpeed = getLoaded() - previouslyLoaded;
            previouslyLoaded = getLoaded();
            lastTime = System.currentTimeMillis();
        }
    }


    public static final int COMPLETED = -1;
    public static final int STOPPED_BY_REMOTE = -2;
    public static final int STOPPED_BY_USER = -3; // didn't work properly that's why not used


    public void reportCompleted() {
        setLoaded(COMPLETED);
        Data.filesSendNotifier.forcePostValue(progress_info);
    }

    public void reportStopped() {
        setLoaded(STOPPED_BY_REMOTE);
        Data.filesSendNotifier.forcePostValue(progress_info);
    }

    public void stop() {
        try {
            socket.getInputStream().close();
            socket.getOutputStream().close();
            socket.close();
        } catch (IOException ignore) {}
        setLoaded(STOPPED_BY_REMOTE); // Passing STOPPED_BY_USER doesn't work properly
        Data.filesSendNotifier.forcePostValue(progress_info);
    }

}
