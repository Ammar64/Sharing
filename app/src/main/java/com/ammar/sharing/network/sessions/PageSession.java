package com.ammar.sharing.network.sessions;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.Server;
import com.ammar.sharing.network.sessions.base.HTTPSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PageSession extends HTTPSession {
    public PageSession(String[] paths) {
        super(paths);
    }

    @Override
    public void GET(Request req, Response res) {
        try {

            String path = req.getPath();
            String file = null;
            String content_type = "*/*";

            switch (path) {
                case "/":
                case "/index.html":
                    file = "index.html";
                    content_type = "text/html";
                    break;
                case "/style.css":
                    file = "style.css";
                    content_type = "text/css";
                    break;
                case "/script.js":
                    file = "script.js";
                    content_type = "text/javascript";
                    break;
                case "/cairo.ttf":
                    file = "cairo.ttf";
                    content_type = "font/ttf";
                    break;
                case "/favicon.ico":
                    file = "icons8-share.svg";
                    content_type = "image/svg+xml";
                    break;
                case "/no-JS":
                    file = "no-JS";
                    content_type = "text/html";
                    break;
                case "/blocked":
                    res.setStatusCode(302);
                    res.setHeader("Location", "/");
                    res.sendResponse();
                    res.close();
                    return;
            }

            if (file != null) {
                res.setHeader("Content-Type", content_type);
                if ("index.html".equals(file)) // read index.html from res
                    res.sendResponse(Utils.readRawRes(R.raw.index));
                else if ("cairo.ttf".equals(file)) // read cairo.ttf from res
                    res.sendResponse(Utils.readRawRes(R.raw.cairo));
                else if ("no-JS".equals(file))
                    generateAnSendNoJSPage(res);
                else
                    res.sendResponse(Utils.readFileFromWebAssets(file));
            } else {
                res.setStatusCode(400);
                res.sendResponse();
            }
        } catch (IOException e) {
            Utils.showErrorDialog("IOException", "PageSession.GET(): " + e.getMessage());
        }
    }


    private void generateAnSendNoJSPage(Response res) {
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
        for (Sharable i : Server.sharablesList) {
            final String downloadLink = "/download/" + i.getUUID();
            final String downloadElement = String.format(Locale.ENGLISH, "        <li><a download href=\"%s\">%s</a></li>\n", downloadLink, i.getName());
            pageBuilder.append(downloadElement);
        }
        pageBuilder.append(pageEnd);

        byte[] pageBytes = pageBuilder.toString().getBytes(StandardCharsets.UTF_8);
        res.sendResponse(pageBytes);
    }
}
