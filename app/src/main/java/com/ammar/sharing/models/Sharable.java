package com.ammar.filescenter.models;

import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.network.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class Sharable {

    protected String uuid;
    protected File file;

    public Sharable(String path) {
        this.file = new File(path);
        this.uuid = UUID.randomUUID().toString();
        this.mimeType = Utils.getMimeType(file.getName());
        if(mimeType.equals("*/*")) mimeType = "application/octet-stream";
    }

    protected Sharable() {
    }

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
    protected String mimeType;


    public String getUUID() {
        return uuid;
    }
    public String getMimeType() {return mimeType;}

    public JSONObject getJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", getUUID());
        jsonObject.put("name", getName());
        jsonObject.put("size", getSize());

        if (mimeType.startsWith("image/"))
            jsonObject.put("type", "img");
        else
            jsonObject.put("type", "file");
        return jsonObject;
    }


    public File getFile() {
        return file;
    }

    public static Sharable getFileWithUUID(String uuid) throws RuntimeException {
        for (Sharable i : Server.filesList) {
            if (uuid.equals(i.getUUID())) {
                return i;
            }
        }
        throw new RuntimeException("FileNotHosted");
    }
}
