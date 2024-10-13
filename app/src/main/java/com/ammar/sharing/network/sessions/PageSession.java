package com.ammar.sharing.network.sessions;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.SharableApp;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.sessions.base.HTTPSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PageSession extends HTTPSession {
    public PageSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        String path = req.getPath();
        if ("/".equals(path)) {
            path = "/pages/index";
        }

        if (path.startsWith("/pages/")) {
            String assetPath = getCorrespondingAssetsPath(path);
            try {
                res.setContentType(Utils.getMimeType(path));
                res.sendResponse(Utils.readFileFromWebAssets(assetPath));

            } catch (IOException e) {
                Utils.showErrorDialog("Requested paths", "Req path: " + path + "\nasset path: " + assetPath);
                sendNotFoundResponse(res);
            }
        } else if ("/no-JS".equals(path)) {
            generateAndSendNoJSPage(res);
        } else if ("/common/almarai_regular.ttf".equals(path)) {
            try {
                res.setContentType("font/ttf");
                res.sendResponse(Utils.readRawRes(R.raw.almarai_regular));
            } catch (IOException e) {
                Utils.showErrorDialog("PagesSession.GET(). IOException.", "Note: error happened when reading raw resources\n" + e.getMessage());
            }
        } else if ("/common/favicon".equals(path)) {
            Bitmap favBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.mipmap.ic_launcher_round, null));
            res.sendBitmapResponse(favBM);
        } else {
            try {
                res.setContentType(Utils.getMimeType(path));
                res.sendResponse(Utils.readFileFromWebAssets(path.substring(1)));
            } catch (IOException e) {
                sendNotFoundResponse(res);
            }
        }
    }

    private String getCorrespondingAssetsPath(String requestedPath) {
        int depth = getPathDepth(requestedPath);
        if (depth == 2) {
            String pageName = requestedPath.substring(requestedPath.lastIndexOf("/") + 1);
            String lang = Locale.getDefault().getLanguage();
            if (!Consts.langsCode.contains(lang)) {
                // default language
                lang = "en";
            }
            return String.format(Locale.ENGLISH, "pages/%s/%s-%s.html", pageName, pageName, lang);
        } else {
            return requestedPath.substring(1); // remove the first / example "/pages/index/something" -> "pages/index/something"
        }
    }

    private void generateAndSendNoJSPage(Response res) {
        Locale locale = Locale.getDefault();
        String dir = switch (TextUtils.getLayoutDirectionFromLocale(locale)) {
            case View.LAYOUT_DIRECTION_RTL -> "rtl";
            case View.LAYOUT_DIRECTION_LTR -> "ltr";
            default ->
                    throw new IllegalStateException("Unexpected value: " + TextUtils.getLayoutDirectionFromLocale(locale));
        };
        final String pageOffset =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"" + locale.toLanguageTag() + "\" dir=\"" + dir + "\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <link rel=\"icon\" type=\"image/x-icon\" href=\"/common/favicon\" />" +
                        "    <title>Sharing</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h2>" + Utils.getRes().getString(R.string.downloads) + "</h2>\n" +
                        "    <table rules=\"all\" border=\"1\" cellpadding=\"10px\">";

        final String pageEnd =
                "    </table>\n" +
                        "</body>\n" +
                        "</html>\n";


        StringBuilder pageBuilder = new StringBuilder();

        pageBuilder.append(pageOffset);

        if (Sharable.sharablesList.isEmpty()) {
            final String noDownloadsText = String.format(Locale.ENGLISH, "<tr>%s</tr>", Utils.getRes().getString(R.string.no_downloads));
            pageBuilder.append(noDownloadsText);
        }
        for (Sharable i : Sharable.sharablesList) {
            final String downloadLink = "/download/" + i.getUUID();
            final String iconSrc = "/get-icon/" + i.getUUID();
            final String downloadElement = String.format(Locale.ENGLISH, "<tr><td><img src=\"%s\" width=\"40px\" /></td><td><a download href=\"%s\">%s</a><br><span dir=\"ltr\">%s</span></td></tr>\n", iconSrc, downloadLink, i.getName(), (i instanceof SharableApp a && (a.hasSplits()) ? "(splits)" : Utils.getFormattedSize(i.getSize()) ));

            pageBuilder.append(downloadElement);
        }
        pageBuilder.append(pageEnd);

        byte[] pageBytes = pageBuilder.toString().getBytes(StandardCharsets.UTF_8);
        res.setContentType("text/html");
        res.sendResponse(pageBytes);
    }

    private int getPathDepth(String path) {
        int count = 0;
        String[] pathParts = path.split("/");
        for (String i : pathParts) {
            if (!i.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void sendNotFoundResponse(Response res) {
        res.setStatusCode(404);
        res.setContentType("text/html");
        res.sendResponse("<h1>404</h1>".getBytes());
        try {
            res.close();
        } catch (IOException e) {
            Utils.showErrorDialog("PageSession.sendNotFoundResponse(). IOException", e.getMessage());
        }
    }
}
