package com.ammar.sharing.custom.io;

import android.os.Bundle;

import com.ammar.sharing.common.Data;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.User;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class ProgressManager {

    private final Sharable sharable;
    private Socket socket;
    private String displayName = null;
    // The other device receiving or sending
    private final User user;

    public enum OP {DOWNLOAD, UPLOAD}

    private final OP op;

    // if `loaded` is -1 it's completed.
    private long loaded;
    // total file size
    private long total;
    // when we started transferring file
    private long startTime;

    // total time taken to finish. -1 if not finished
    private long totalFinishTime = -1;

    private int index;
    private final UUID uuid;
    private UUID fileUUID = null;

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
    public ProgressManager(Sharable sharable, Socket socket, long total, User user, OP opType) {
        this.uuid = UUID.randomUUID();
        this.sharable = sharable;
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
        startTime = System.currentTimeMillis();
    }


    public long getTotal() {
        return total;
    }

    public String getFileName() {
        return sharable.getFileName();
    }

    public String getDisplayName() {
        return displayName == null ? sharable.getName() : displayName;
    }

    public Sharable getSharable() {
        return sharable;
    }

    public String getFileType() {
        return Utils.getMimeType(sharable.getName());
    }

    public User getUser() {
        return user;
    }

    public long getLoaded() {
        return loaded;
    }

    public UUID getFileUUID() {
        return this.fileUUID;
    }

    public UUID getProgressUUID() {
        return this.uuid;
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

    // totalFinishTime is -1 if not finished
    public long getTotalTime() {
        return totalFinishTime;
    }

    public void setFileUUID(UUID uuid) {
        this.fileUUID = uuid;
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
        totalFinishTime = System.currentTimeMillis() - startTime;
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
