package com.ammar.sharing.network.sessions;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.exceptions.BadRequestException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UserSession extends HTTPSession {
    public UserSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        try {
            if ("/get-user-info".equals(req.getPath())) {
                JSONObject userJson = new JSONObject();
                userJson.put("id", user.getId());
                userJson.put("username", user.getName());
                res.setStatusCode(200);
                res.sendResponse(userJson.toString().getBytes());
            }
        } catch (JSONException e) {
            Utils.showErrorDialog("UserSession.GET(). JSONException:", e.getMessage() + ".\n path: " + req.getPath());
            res.setStatusCode(400); // if it throws this exception json sent is invalid.
            res.sendResponse();
        }
    }

    @Override
    public void POST(Request req, Response res) {
        try {
            if ("/update-user-name".equals(req.getPath())) {
                JSONObject jsonReq = new JSONObject(req.getBody());
                String username = jsonReq.getString("username");
                user.setName(username);

                JSONObject jsonRes = new JSONObject();
                jsonRes.put("changed", true);
                jsonRes.put("username", user.getName());
                jsonRes.put("userID", user.getId());
                res.setContentType("application/json");
                res.sendResponse(jsonRes.toString().getBytes());
            }
        } catch (BadRequestException e) {
            Utils.showErrorDialog("UserSession.POST(). BadRequestException:", e.getMessage() + ".\n path: " + req.getPath());
            res.setStatusCode(400);
            res.sendResponse();
        } catch (JSONException e) {
            Utils.showErrorDialog("UserSession.POST(). JSONException:", e.getMessage() + ".\n path: " + req.getPath());
            res.setStatusCode(400);
            res.sendResponse();
        } catch (IOException e) {
            Utils.showErrorDialog("UserSession.POST(). IOException:", e.getMessage() + ".\n path: " + req.getPath());
            res.setStatusCode(500);
            res.sendResponse();
        }
    }
}
