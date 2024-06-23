package com.ammar.filescenter.services.network.sessions;

import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.network.Request;
import com.ammar.filescenter.services.network.Response;
import com.ammar.filescenter.services.network.exceptions.BadRequestException;
import com.ammar.filescenter.services.network.sessions.base.HTTPSession;

import org.json.JSONException;
import org.json.JSONObject;

public class UserSession extends HTTPSession {
    public UserSession(String[] paths) {
        super(paths);
    }

    @Override
    public void POST(Request req, Response res) {
        try {
            if ("/update-user-name".equals(req.getPath())) {
                JSONObject jsonReq = new JSONObject(req.getBody());
                String username = jsonReq.getString("username");
                user.setName(username);
            }
        } catch (BadRequestException e) {
            Utils.showErrorDialog("UserSession.POST(). BadRequestException:", e.getMessage() + ".\n path: " + req.getPath());
        } catch (JSONException e) {
            Utils.showErrorDialog("UserSession.POST(). JSONException:", e.getMessage() + ".\n path: " + req.getPath());
        }
    }
}
