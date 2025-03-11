package com.ammar.sharing.activities.AddAppsAndFilesActivity.data;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.core.content.res.ResourcesCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;

public class AppData {
    public AppData(String packageName, boolean hasSplits, String label, Drawable icon) {
        this.packageName = packageName;
        this.hasSplits = hasSplits;
        this.label = label;
        int size = (int) Utils.dpToPx(50);
        if( hasSplits ) {
            Drawable[] layers = new Drawable[2];
            layers[0] = icon;
            layers[1] = ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.banner_splits, null);
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            this.icon = Bitmap.createScaledBitmap( Utils.drawableToBitmap(layerDrawable), size, size, true );
        } else {
            this.icon = Bitmap.createScaledBitmap( Utils.drawableToBitmap(icon), size, size, true );
        }
    }

    public final String packageName;
    public final Bitmap icon;
    public final String label;
    public final boolean hasSplits;
    public boolean isChecked = false;
}
