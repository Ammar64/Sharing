package com.ammar.sharing.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.core.content.res.ResourcesCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SharableApp extends Sharable {
    private PackageManager pm;
    private ApplicationInfo appInfo;

    public SharableApp(Context context, String package_id) throws PackageManager.NameNotFoundException {
        // get pm and app info
        this.pm = context.getPackageManager();
        this.appInfo = pm.getApplicationInfo(package_id, 0);
        app_name = appInfo.loadLabel(pm).toString();
        // construct base class
        fileName = app_name + ".apk";
        super.uuid = UUID.randomUUID().toString();

        // check for splits
        String[] splitsDirs = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            splitsDirs = appInfo.splitPublicSourceDirs;
        }
        if (splitsDirs != null) {
            splits = new Sharable[splitsDirs.length];
            for (int i = 0; i < splitsDirs.length; i++) {
                splits[i] = new Sharable(splitsDirs[i]);
            }
            _hasSplits = true;
        }


        super.file = new File(appInfo.publicSourceDir);
        fileSize = super.file.length();
        this.mimeType = "application/vnd.android.package-archive";
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
            for (Sharable i : splits) {
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

    public Sharable getSplitWithUUID(String uuid) {
        for (Sharable i : splits) {
            if (uuid.equals(i.getUUID())) {
                return i;
            }
        }
        throw new RuntimeException("SplitNotFound");
    }

    private Drawable icon = null;

    public Drawable getIcon() {
        if (icon == null) {
            Drawable appIcon = appInfo.loadIcon(pm);
            if (hasSplits()) {
                icon = new LayerDrawable(new Drawable[]{appIcon, ResourcesCompat.getDrawable(Utils.getRes(), R.drawable.banner_splits, null)});
            } else {
                icon = appIcon;
            }
        }
        return icon;
    }

    @Override
    public Bitmap getBitmapIcon() {
        return Utils.drawableToBitmap(getIcon());
    }

    private boolean _hasSplits = false;
    Sharable[] splits = null;

    public boolean hasSplits() {
        return _hasSplits;
    }

    public Sharable[] getSplits() {
        return splits;
    }
}

