package com.ammar.filescenter.services.models;

import androidx.documentfile.provider.DocumentFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class Upload {


    private String uuid;
    private DocumentFile file;

    public Upload(String path) {
        this.file = DocumentFile.fromFile(new File(path));
        this.uuid = UUID.randomUUID().toString();
    }

    public String getFileName() {
        return file.getName();
    }

    public String getFilePath() {
        return file.getUri().getPath();
    }

    public long getSize() {
        return file.length();
    }

    public String getMimeType() {
        return file.getType();
    }

    public String getUUID() {
        return uuid;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("uuid", getUUID());
        jsonObject.accumulate("name", getFileName());
        jsonObject.accumulate("size", getSize());
        return jsonObject;
    }
}
