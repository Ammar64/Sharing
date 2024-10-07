package com.ammar.sharing.network.sessions;

import androidx.annotation.Keep;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.sessions.base.HTTPSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Keep
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
        } else if ("/common/cairo.ttf".equals(path)) {
            try {
                res.setContentType("font/ttf");
                res.sendResponse(Utils.readRawRes(R.raw.cairo));
            } catch (IOException e) {
                Utils.showErrorDialog("PagesSession.GET(). IOException.", "Note: error happened when reading raw resources\n" + e.getMessage());
            }
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
        final String pageOffset =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <title>Sharing</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h3>Downloads</h3>\n" +
                        "    <ul>";

        final String pageEnd =
                "    </ul>\n" +
                        "</body>\n" +
                        "</html>\n";


        StringBuilder pageBuilder = new StringBuilder();

        pageBuilder.append(pageOffset);
        for (Sharable i : Sharable.sharablesList) {
            final String downloadLink = "/download/" + i.getUUID();
            final String downloadElement = String.format(Locale.ENGLISH, "        <li><a download href=\"%s\">%s</a></li>\n", downloadLink, i.getName());
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
