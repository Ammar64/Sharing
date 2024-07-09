package com.ammar.filescenter.activities.AddFilesActivity;

import com.ammar.filescenter.common.Utils;

public class FileTypeName {
    private FileTypeName(){};

    public static String getFileTypeName(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1);
        return ext.toUpperCase();
    }
}
