package com.ammar.filescenter.network.sessions;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.network.Request;
import com.ammar.filescenter.network.Response;
import com.ammar.filescenter.network.sessions.base.HTTPSession;

import java.io.IOException;

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
                case "index.html":
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
                case "/dv.png":
                    file = "dv.png";
                    content_type = "image/png";
                    break;
                case "/favicon.ico":
                    file = "icons8-share.svg";
                    content_type = "image/svg+xml";
                    break;
            }

            if( file != null ) {
                res.setHeader("Content-Type", content_type);
                if( file.equals("dv.png") ) {
                    res.sendResponse(Utils.readRawRes(R.raw.dv));
                } else {
                    res.sendResponse(Utils.readFileFromWebAssets(file));
                }
            } else {
                res.setStatusCode(400);
                res.sendResponse();
            }
        } catch (IOException e) {
            Utils.showErrorDialog( "IOException", "PageSession.GET(): " + e.getMessage() );
        }
    }
}
