package com.ammar.sharing.activities.AddAppsAndFilesActivity.data;

import android.net.Uri;

public class MediaData {
    public String name;
    public Uri data;
    public long size;
    public boolean isChecked = false;

    public MediaData(String name, long size, Uri data) {
        this.name = name;
        this.size = size;
        this.data = data;
    }
}
