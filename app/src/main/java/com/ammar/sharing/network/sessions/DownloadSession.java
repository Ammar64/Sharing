package com.ammar.sharing.network.sessions;

import android.util.Log;

import com.ammar.sharing.common.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.SharableApp;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.sessions.base.HTTPSession;

import org.json.JSONArray;
import org.json.JSONException;

import java.nio.charset.StandardCharsets;

public class DownloadSession extends HTTPSession {
    public DownloadSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        // the length of both /download/ and /get-icon/ is 10 anyway
        String requestedUUID = req.getPath().substring(10);
        try {
            if (req.getPath().startsWith("/download/")) {
                Sharable file = Sharable.getFileWithUUID(requestedUUID);
                if (!(file instanceof SharableApp)) {
                    long start = req.getStartRange();
                    if (start == -1)
                        res.sendFileResponse(file, user);
                    else res.resumePausedFileResponse(file, start, user);
                } else {
                    SharableApp app = (SharableApp) file;
                    if (app.hasSplits()) {
                        Sharable[] app_splits = app.getSplits();
                        Sharable[] app_files = new Sharable[app_splits.length + 1];

                        // app base.apk must be the first file because it will be the name of the zip.
                        app_files[0] = app;
                        for (int i = 1; i < app_files.length; i++) {
                            app_files[i] = app_splits[i - 1];
                        }
                        res.sendApksFileResponse(app_files ,user);
                    } else {
                        long start = req.getStartRange();
                        if (start == -1)
                            res.sendFileResponse(app, user);
                        else res.resumePausedFileResponse(app, start, user);
                    }
                }
            } else if (req.getPath().equals("/available-downloads")) {
                byte[] available = getFilesJson();
                res.sendResponse(available);
            } else if (req.getPath().startsWith("/get-icon/")) {
                Sharable file = Sharable.getFileWithUUID(requestedUUID);
                res.sendBitmapResponse(file.getBitmapIcon());
            }
        }
        catch (RuntimeException e) {
            Log.i("MYLOG", "FileNotHosted");
            if ("FileNotHosted".equals(e.getMessage())) {
                res.setHeader("Content-Type", "text/html");
                res.setHeader("Content-Disposition", "inline");
                res.sendResponse("File not hosted".getBytes(StandardCharsets.UTF_8));
            } else {
                Utils.showErrorDialog("DownloadSession.GET(). RuntimeException", e.getMessage() + "\n path: " + req.getPath());
            }
        } catch (JSONException e) {
            Utils.showErrorDialog("DownloadSession.GET(). JSONException:", e.getMessage() + ".\n path: " + req.getPath());
            res.setStatusCode(500);
            res.sendResponse();
        }


    }

    private byte[] getFilesJson() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Sharable i : Sharable.sharablesList) {
            jsonArray.put(i.getJSON());
        }
        return jsonArray.toString().getBytes();
    }
}
