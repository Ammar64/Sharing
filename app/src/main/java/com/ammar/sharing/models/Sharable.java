package com.ammar.sharing.models;

import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.res.ResourcesCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.FileUtils;
import com.ammar.sharing.common.utils.Utils;

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
    protected UUID uuid;
    protected File file;
    protected Uri uri;

    protected ContentResolver resolver;
    protected String fileName;
    protected long fileSize;
    private final boolean isUri;

    public Sharable(String path) {
        this.file = new File(path);
        this.uuid = UUID.randomUUID();
        fileName = file.getName();
        fileSize = file.length();
        this.mimeType = Utils.getMimeType(fileName);
        if (mimeType.equals("*/*")) mimeType = "application/octet-stream";
        isUri = false;
    }

    public Sharable(ContentResolver resolver, Uri uri) {
        this.uri = uri;
        this.resolver = resolver;
        this.uuid = UUID.randomUUID();
        fileName = FileUtils.getFileName(resolver, uri);
        try (AssetFileDescriptor assetFileDescriptor = resolver.openAssetFileDescriptor(uri, "r")) {
            fileSize = assetFileDescriptor.getLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.mimeType = Utils.getMimeType(fileName);
        if (mimeType.equals("*/*")) mimeType = "application/octet-stream";
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

    public Bitmap getBitmapIcon() {
        String mimeType = this.getMimeType();
        Bitmap imageBM;
        if (mimeType.startsWith("image/")) {
            imageBM = FileUtils.decodeSampledSharableImage(this, 256, 256);
        } else if (mimeType.startsWith("video/")) {
            imageBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.icon_video, null));
        } else if (mimeType.startsWith("audio/")) {
            imageBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.icon_audio, null));
        } else if("application/pdf".equals(mimeType)) {
            imageBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.icon_pdf, null));
        } else if (Utils.isDocumentType(mimeType)) {
            imageBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.icon_document, null));
        } else if ("application/vnd.android.package-archive".equals(mimeType)) {
            if(isUri) {
                return Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.icon_archive, null));
            } else {
                PackageManager pm = Utils.getPm();
                PackageInfo pi = pm.getPackageArchiveInfo(getFilePath(), 0);
                if( pi != null ) {
                    ApplicationInfo appInfo = pi.applicationInfo;
                    appInfo.publicSourceDir = getFilePath();
                    appInfo.sourceDir = getFilePath();
                    return Utils.drawableToBitmap(appInfo.loadIcon(pm));
                } else {
                    return Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.icon_archive, null));
                }
            }
        } else {
            imageBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.icon_file, null));
        }
        return imageBM;
    }

    protected String mimeType;

    public UUID getUUID() {
        return uuid;
    }

    public String getMimeType() {
        return mimeType;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", getUUID().toString());
        jsonObject.put("name", getName());
        jsonObject.put("size", getSize());

        if (mimeType.startsWith("image/"))
            jsonObject.put("type", "img");
        else if (mimeType.startsWith("video/"))
            jsonObject.put("type", "video");
        return jsonObject;
    }

    public boolean isUri() {
        return isUri;
    }

    public File getFile() {
        if (isUri) throw new RuntimeException("Sharable is not a file");
        return file;
    }

    public Uri getUri() {
        if (!isUri) throw new RuntimeException("Sharable is not a Uri");
        return uri;
    }

    public InputStream openInputStream() throws FileNotFoundException {
        if (isUri) {
            return resolver.openInputStream(uri);
        } else {
            return new FileInputStream(file);
        }
    }

    public static Sharable getFileWithUUID(String uuid) throws RuntimeException {
        for (Sharable i : sharablesList) {
            if (uuid.equals(i.getUUID().toString())) {
                return i;
            }
        }
        throw new RuntimeException("FileNotHosted");
    }

    public static boolean sharableUUIDExists(String uuid) {
        for (Sharable i : sharablesList) {
            if (i.getUUID().toString().equals(uuid)) return true;
        }
        return false;
    }
}
