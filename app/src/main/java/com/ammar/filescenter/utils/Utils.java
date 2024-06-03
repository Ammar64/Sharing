package com.ammar.filescenter.utils;

import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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


    public static String readLineUTF8(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != -1 ; c = inputStream.read()) {
            if( c == '\n' ) break;
            if( c == '\r' ) {
                if(inputStream.read() == '\n' ) break;
                else throw new RuntimeException("No \\n after \\r");
            }
            byteArrayOutputStream.write(c);
        }
        if (c == -1 && byteArrayOutputStream.size() == 0) {
            return null;
        }
        String line = byteArrayOutputStream.toString("UTF-8");
        return line;
    }

}
