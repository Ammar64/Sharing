package com.ammar.sharing.network.utils;

import com.ammar.sharing.common.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebAppUtils {
    private WebAppUtils() {
    }

    private static ArrayList<String> webAppPathsList;
    private static ArrayList<String> webAppRoutesList;

    private static boolean _isInit = false;

    private static ExecutorService sExecutor;

    public static void init(ExecutorService initExecutor) {
        sExecutor = initExecutor;
        if (!_isInit) {
            try (InputStream input = Utils.getAssetManager().open("web_app_files_list.txt")) {
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

            try(InputStream input = Utils.getAssetManager().open("web_routes_list.txt")) {
                int size = input.available();
                byte[] buffer = new byte[size];
                input.read(buffer);
                String routesListsStr = new String(buffer);
                String[] routesList = routesListsStr.split("\n");
                webAppRoutesList = new ArrayList<>(Arrays.asList(routesList));
                Collections.sort(webAppRoutesList);
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
        waitInitExecutor();
        return Collections.binarySearch(webAppPathsList, path.substring(1)) >= 0;
    }

    public static boolean webAppRouteExists(String path) {
        waitInitExecutor();
        return Collections.binarySearch(webAppRoutesList, path) >= 0;
    }

    private static void waitInitExecutor() {
        try {
            boolean jobDone = sExecutor.awaitTermination(7000, TimeUnit.MILLISECONDS);
            if(!jobDone) throw new RuntimeException("WebAppUtils.webAppPathExists(). problem happened with init executor");
        } catch (InterruptedException ignore) {}
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
