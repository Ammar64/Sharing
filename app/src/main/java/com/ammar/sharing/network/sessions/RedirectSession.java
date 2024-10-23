package com.ammar.sharing.network.sessions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.sessions.base.HTTPSession;

public class RedirectSession extends HTTPSession {
    public static final HashMap<String, String> redirectMap = new HashMap<>();
    static {
        redirectMap.put("/play", "/pages/play");
    }

    public RedirectSession(User user) {
        super(user);
    }
    @Override
    public void GET(Request req, Response res) {
        String path = req.getPath();
        // remove the / at the end if present /play/ -> /play
        if(path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }

        if( redirectMap.containsKey(path) ) {
            String location = redirectMap.get(path);
            res.setStatusCode(301); // moved permanently
            res.setHeader("Content-Length", "0");
            res.setHeader("Location", location);
            res.sendResponse();
        } else {
            res.setStatusCode(500);
            res.sendResponse();
            try {
                res.close();
            } catch (IOException e) {
                Utils.showErrorDialog("RedirectSession.GET(). IOException", e.getMessage());
            }
        }
    }
}
