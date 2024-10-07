package com.ammar.sharing.models;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.ammar.sharing.common.FileUtils;
import com.ammar.sharing.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.UUID;

public class Sharable {

    public static final LinkedList<Sharable> sharablesList = new LinkedList<>();
    protected String uuid;
    protected File file;
    protected Uri uri;

    protected ContentResolver resolver;
    protected String fileName;
    protected long fileSize;
    private final boolean isUri;
    public Sharable(String path) {
        this.file = new File(path);
        this.uuid = UUID.randomUUID().toString();
        fileName = file.getName();
        fileSize = file.length();
        this.mimeType = Utils.getMimeType(fileName);
        if(mimeType.equals("*/*")) mimeType = "application/octet-stream";
        isUri = false;
    }

    public Sharable(ContentResolver resolver, Uri uri) throws FileNotFoundException {
        this.uri = uri;
        this.resolver = resolver;
        this.uuid = UUID.randomUUID().toString();
        fileName = FileUtils.getFileName(resolver, uri);
        try( AssetFileDescriptor assetFileDescriptor = resolver.openAssetFileDescriptor(uri, "r")) {
            fileSize = assetFileDescriptor.getLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.mimeType = Utils.getMimeType(fileName);
        if(mimeType.equals("*/*")) mimeType = "application/octet-stream";
        isUri = true;
    }

    protected Sharable() {
        isUri = false;
    }

    // this method is meant to be overridden.
    public String getName() {
        return getFileName();
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return file.getPath();
    }

    public long getSize() {
        return fileSize;
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
        else if( mimeType.startsWith("video/") )
            jsonObject.put("type", "video");
        return jsonObject;
    }

    public File getFile() {
        if( isUri ) throw new RuntimeException("Sharable is not a file");
        return file;
    }
    public InputStream openInputStream() throws FileNotFoundException {
        if( isUri ) {
            return resolver.openInputStream(uri);
        } else {
            return new FileInputStream(file);
        }
    }

    public static Sharable getFileWithUUID(String uuid) throws RuntimeException {
        for (Sharable i : sharablesList) {
            if (uuid.equals(i.getUUID())) {
                return i;
            }
        }
        throw new RuntimeException("FileNotHosted");
    }

    public static boolean sharableUUIDExists(String uuid) {
        for( Sharable i : sharablesList) {
            if( i.getUUID().equals(uuid) ) return true;
        }
        return false;
    }
}
