package com.ammar.filescenter.services.models;

import android.webkit.MimeTypeMap;

import com.ammar.filescenter.services.network.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class Transferable {

    protected String uuid;
    protected File file;

    public Transferable(String path) {
        this.file = new File(path);
        this.uuid = UUID.randomUUID().toString();
    }

    protected Transferable() {}

    // this method is meant to be overridden.
    public String getName() {
        return file.getName();
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

    public String getMimeType() {
        String type = null;
        final String url = file.toString();
        final String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        if (type == null) {
            type = "application/octet-stream"; // fallback type.
        }
        return type;
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


    public File getFile() {
        return file;
    }

    public static Transferable getFileWithUUID(String uuid) throws RuntimeException {
        for (Transferable i : Server.filesList) {
            if (uuid.equals(i.getUUID())) {
                return i;
            }
        }
        throw new RuntimeException("FileNotHosted");
    }
}
