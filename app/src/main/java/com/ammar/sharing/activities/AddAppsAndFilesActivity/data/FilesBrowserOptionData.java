package com.ammar.sharing.activities.AddAppsAndFilesActivity.data;


import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class FilesBrowserOptionData {
    public FilesBrowserOptionData(@DrawableRes int icon, String title,  String desc, @Nullable String path) {
        this.mIcon = icon;
        this.mTitle = title;
        this.mDesc = desc;
        this.mPath = path;
    }

    public @DrawableRes int mIcon;
    public String mTitle;
    public String mDesc;
    @Nullable
    public String mPath;
}
