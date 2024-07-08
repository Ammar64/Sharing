package com.ammar.filescenter.network.sessions;

import android.graphics.Bitmap;
import android.util.Log;

import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.models.Transferable;
import com.ammar.filescenter.models.TransferableApp;
import com.ammar.filescenter.network.Request;
import com.ammar.filescenter.network.Response;
import com.ammar.filescenter.network.Server;
import com.ammar.filescenter.network.sessions.base.HTTPSession;

import org.json.JSONArray;
import org.json.JSONException;

import java.nio.charset.StandardCharsets;

public class DownloadSession extends HTTPSession {
    public DownloadSession(String[] paths) {
        super(paths, true);
    }

    @Override
    public void GET(Request req, Response res) {
        // the length of both /download/ and /get-icon/ is 10 anyway
        String requestedUUID = req.getPath().substring(10);
        try {
            if (req.getPath().startsWith("/download/")) {
                Transferable file = Transferable.getFileWithUUID(requestedUUID);
                if (!(file instanceof TransferableApp)) {
                    long start = req.getStartRange();
                    if (start == -1)
                        res.sendFileResponse(file, user);
                    else res.resumePausedFileResponse(file, start, user);
                } else {
                    TransferableApp app = (TransferableApp) file;
                    if (app.hasSplits()) {
                        Transferable[] app_splits = app.getSplits();
                        Transferable[] app_files = new Transferable[app_splits.length + 1];

                        // app base.apk must be the first file because it will be the name of the zip.
                        app_files[0] = app;
                        for (int i = 1; i < app_files.length; i++) {
                            app_files[i] = app_splits[i - 1];
                        }
                        res.sendZippedFilesResponse(app_files, user);
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
                Transferable file = Transferable.getFileWithUUID(requestedUUID);
                if (file instanceof TransferableApp) {
                    TransferableApp app = (TransferableApp) file;
                    Bitmap appIconBM = Utils.drawableToBitmap(app.getIcon());
                    res.sendBitmapResponse(appIconBM);
                } else {
                    if( file.getMimeType().startsWith("image/") ) {
                        res.sendFileResponse(file, false, user);
                    } else{
                        res.setStatusCode(400);
                        res.sendResponse();
                    }
                }
            }
        } catch (RuntimeException e) {
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
        for (Transferable i : Server.filesList) {
            jsonArray.put(i.getJSON());
        }
        return jsonArray.toString().getBytes();
    }
}
