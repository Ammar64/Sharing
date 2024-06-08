package com.ammar.filescenter.services.models;

import androidx.documentfile.provider.DocumentFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class Upload {


    protected String uuid;
    protected DocumentFile file;

    public Upload(String path) {
        this.file = DocumentFile.fromFile(new File(path));
        this.uuid = UUID.randomUUID().toString();
    }

    protected Upload() {}
    public String getName() {
        return file.getName();
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
        jsonObject.put("uuid", getUUID());
        jsonObject.put("name", getName());
        jsonObject.put("size", getSize());
        jsonObject.put("type", "file");
        return jsonObject;
    }


}
