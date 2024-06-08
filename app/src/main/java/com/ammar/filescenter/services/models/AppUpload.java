package com.ammar.filescenter.services.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppUpload extends Upload {
    public AppUpload(Context context, String package_id) throws PackageManager.NameNotFoundException {
        // get pm and app info
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = pm.getApplicationInfo(package_id, 0);
        app_name = appInfo.loadLabel(pm).toString();

        // construct base class
        super.uuid = UUID.randomUUID().toString();

        // check for splits
        String[] splitsDirs = appInfo.splitPublicSourceDirs;
        if (splitsDirs != null) {
            splits = new Upload[splitsDirs.length];
            for (int i = 0; i < splitsDirs.length; i++) {
                splits[i] = new Upload(splitsDirs[i]);
            }
            _hasSplits = true;
        }

        super.file = DocumentFile.fromFile(new File(appInfo.publicSourceDir));
    }

    private void AddFileToZipArchive(ZipOutputStream zout, String path) throws IOException {
        String name = path.substring(path.lastIndexOf("/"));
        ZipEntry zipEntry = new ZipEntry(name);
        FileInputStream fin = new FileInputStream(path);
        zout.putNextEntry(zipEntry);

        byte[] buffer = new byte[8192];
        int count;
        while ((count = fin.read(buffer, 0, buffer.length)) != -1) {
            zout.write(buffer, 0, count);
        }
        fin.close();
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject jsonObject = super.getJSON();
        jsonObject.put("type", "app");
        jsonObject.put("hasSplits", _hasSplits);
        if (_hasSplits) {
            JSONArray splitsArray = new JSONArray();
            for (Upload i : splits) {
                splitsArray.put(i.getJSON());
            }
            jsonObject.put("splits", splitsArray);
        }
        return jsonObject;
    }


    private final String app_name;

    @Override
    public String getName() {
        return app_name;
    }

    @Override
    public String getFileName() {
        return app_name + ".apk";
    }
    public Upload getSplitWithUUID(String uuid) {
        for (Upload i : splits) {
            if (uuid.equals(i.getUUID())) {
                return i;
            }
        }
        throw new RuntimeException("SplitNotFound");
    }

    private boolean _hasSplits = false;
    Upload[] splits = null;

    public boolean hasSplits() {
        return _hasSplits;
    }

    public Upload[] getSplits() {
        return splits;
    }
}

