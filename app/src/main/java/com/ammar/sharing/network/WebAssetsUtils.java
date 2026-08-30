package com.ammar.sharing.network;

import android.content.res.AssetManager;
import android.util.Log;

import com.ammar.sharing.common.Global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WebAssetsUtils {
    private static AssetManager assetManager;
    // Should only be called once on install
    // AppShareAny will handle when to call this method
    public static void init(AssetManager assetManager) {
            WebAssetsUtils.assetManager = assetManager;
            copyFileOrDir("web_app");
            copyFileOrDir("web_app_files_list.txt");
            copyFileOrDir("web_app_routes_list.txt");
    }

    private static void copyFileOrDir(String path) {
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {;
                File dir = new File(Global.WEB_ASSETS_PATH, path);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private static void copyFile(String filename) {

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            out = new FileOutputStream(new File(Global.WEB_ASSETS_PATH, filename));

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }
}
