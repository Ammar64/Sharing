package com.ammar.sharing.network.utils;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class WebAppUtils {
    private WebAppUtils() {}
    private static ArrayList<String> webAppPathsList;

    private static boolean _isInit = false;
    public static void init() {
        if( !_isInit ) {
            try {
                InputStream input = Utils.getAssetManager().open("web_app_files_list.txt");
                int size = input.available();
                byte[] buffer = new byte[size];
                input.read(buffer);
                String filesPathsListStr = new String(buffer);
                String[] filesPathsArray = filesPathsListStr.split("\n");
                webAppPathsList = new ArrayList<>(Arrays.asList(filesPathsArray));
                Collections.sort(webAppPathsList);
            } catch (IOException e) {
                Utils.showErrorDialog("WebAppUtils.init()", e.getMessage());
            }
            _isInit = true;
        }
    }

    public static String getWebResourcePath(String requestedPath) {
        return "web_app" + requestedPath;
    }

    public static boolean webAppPathExists(String path) {
        return Collections.binarySearch(webAppPathsList, path.substring(1)) >= 0;
    }
    public static byte[] readFileFromWebAssets(String filepath) {
        try (InputStream input = Utils.getAssetManager().open(filepath)) {
            int size = input.available();
            byte[] content = new byte[size];
            int numBytes = input.read(content);
            if (numBytes != size) {
                throw new RuntimeException("Error reading file");
            }
            return content;
        } catch (IOException e) {
            Utils.showErrorDialog("WebAppUtils.readFileFromWebAssets(). IOException", "Failed to read from assets");
            return new byte[0];
        }
    }
}
