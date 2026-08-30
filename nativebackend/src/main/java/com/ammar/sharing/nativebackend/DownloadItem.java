package com.ammar.sharing.nativebackend;

import androidx.annotation.Keep;

@SuppressWarnings("JavaJniMissingFunction")
@Keep
public class DownloadItem {
    private final String name;
    private final long size;
    private final String mimeType;

    public DownloadItem(String name, String mimeType, long size) {
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }
}
