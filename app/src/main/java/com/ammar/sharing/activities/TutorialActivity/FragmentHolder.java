package com.ammar.filescenter.activities.TutorialActivity;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class FragmentHolder {
    @DrawableRes
    private int backgroundImageId;

    @DrawableRes
    private int iconId;

    @StringRes
    private int titleId;

    @StringRes
    private int descId;

    public FragmentHolder(int backgroundImageId, int iconId, int titleId, int descId) {
        this.backgroundImageId = backgroundImageId;
        this.iconId = iconId;
        this.titleId = titleId;
        this.descId = descId;
    }

    public int getBackgroundImageId() {
        return backgroundImageId;
    }

    public int getIconId() {
        return iconId;
    }

    public int getTitleId() {
        return titleId;
    }

    public int getDescId() {
        return descId;
    }
}
