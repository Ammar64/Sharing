package com.ammar.filescenter.services.network.sessions;

import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.network.Request;
import com.ammar.filescenter.services.network.Response;
import com.ammar.filescenter.services.network.sessions.base.HTTPSession;

import java.io.IOException;
import java.util.ArrayList;

public class PageSession extends HTTPSession {
    public PageSession(String[] paths) {
        super(paths);
    }

    @Override
    public void GET(Request req, Response res) {
        try {

            String path = req.getPath();
            String file = null;

            switch (path) {
                case "/":
                case "index.html":
                    file = "index.html";
                    break;
                case "/style.css":
                    file = "style.css";
                    break;
                case "/script.js":
                    file = "script.js";
                    break;
                case "/dv.png":
                    file = "dv.png";
                    break;
            }

            if( file != null ) res.sendResponse(Utils.readFileFromAssets(file));
            else {
                res.setStatusCode(400);
                res.sendResponse();
            }
        } catch (IOException e) {
            Utils.showErrorDialog( "IOException", "PageSession.GET(): " + e.getMessage() );
        }
    }
}
