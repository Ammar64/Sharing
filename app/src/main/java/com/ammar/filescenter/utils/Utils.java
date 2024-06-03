package com.ammar.filescenter.utils;

import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.Locale;

public class Utils {
    public static String getFormattedSize(long size) {
        double s = size;
        String[] levels = {"B", "KB", "MB", "GB", "TB", "PB"};
        int level = 0;
        boolean isGood = false;
        while (!isGood) {
            if (s > 1200 && level < levels.length) {
                s /= 1024;
                level++;
            } else {
                isGood = true;
            }
        }
        return String.format(Locale.ENGLISH, "%.2f %s", s, levels[level]);
    }

    public static float dpToPx(float dp, DisplayMetrics metrics) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                metrics
        );
    }

}
