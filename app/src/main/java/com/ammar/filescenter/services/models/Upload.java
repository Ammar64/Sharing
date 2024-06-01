package com.ammar.filescenter.activities.MainActivity.models;

import static java.io.File.separatorChar;

import java.io.File;
import java.util.UUID;

public class Upload {
    private String uuid;
    private File file;

    public Upload(String path) {
        this.file = new File(path);
        this.uuid = UUID.randomUUID().toString();
    }

    public String getFileName() {
        return file.getName();
    }

    public String getFilePath() {
        return file.getPath();
    }

    public long getSize() {
        return file.length();
    }
}
