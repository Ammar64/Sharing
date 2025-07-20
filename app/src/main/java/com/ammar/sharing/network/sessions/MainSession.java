package com.ammar.sharing.network.sessions;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.utils.WebAppUtils;



// Don't add to the server
public class MainSession extends HTTPSession {
    public MainSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        String path = req.getPath();
        if ("/".equals(path)) {
            String indexHTMLWebAppPath = WebAppUtils.getWebResourcePath("/index.html");
            byte[] content = WebAppUtils.readFileFromWebAssets(indexHTMLWebAppPath);
            res.setStatusCode(200);
            res.setContentType("text/html");
            res.sendResponse(content);
        } else if( WebAppUtils.webAppPathExists(path) ) {
            String webAppPath = WebAppUtils.getWebResourcePath(path);
            byte[] content = WebAppUtils.readFileFromWebAssets(webAppPath);
            res.setStatusCode(200);
            res.setContentType(Utils.getMimeType(webAppPath));
            res.sendResponse(content);
        } else {
            String indexHTMLWebAppPath = WebAppUtils.getWebResourcePath("/index.html");
            byte[] content = WebAppUtils.readFileFromWebAssets(indexHTMLWebAppPath);
            res.setStatusCode(404);
            res.setContentType("text/html");
            res.sendResponse(content);
        }
    }
}
