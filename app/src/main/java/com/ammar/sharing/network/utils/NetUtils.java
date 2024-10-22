package com.ammar.sharing.network.utils;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.Response;

import java.util.Locale;

public class NetUtils {
    private NetUtils() {}
    public static String getCorrespondingAssetsPath(String requestedPath, Response res) {
        int depth = getPathDepth(requestedPath);
        if (depth == 2) {
            res.setContentType("text/html");
            String pageName = requestedPath.substring(requestedPath.lastIndexOf("/") + 1);
            String lang = Locale.getDefault().getLanguage();
            if (!Consts.langsCode.contains(lang)) {
                // default language
                lang = "en";
            }
            return String.format(Locale.ENGLISH, "pages/%s/%s-%s.html", pageName, pageName, lang);
        } else {
            res.setContentType(Utils.getMimeType(requestedPath));
            return requestedPath.substring(1); // remove the first / example "/pages/index/something" -> "pages/index/something"
        }
    }



    private static int getPathDepth(String path) {
        int count = 0;
        String[] pathParts = path.split("/");
        for (String i : pathParts) {
            if (!i.isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
