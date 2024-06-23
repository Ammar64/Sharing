package com.ammar.filescenter.services.network.sessions;

import android.util.Log;

import com.ammar.filescenter.services.models.Transferable;
import com.ammar.filescenter.services.models.TransferableApp;
import com.ammar.filescenter.services.network.Request;
import com.ammar.filescenter.services.network.Response;
import com.ammar.filescenter.services.network.sessions.base.HTTPSession;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class DownloadSession extends HTTPSession {
    public DownloadSession(String[] paths) {
        super(paths, true);
    }

    @Override
    public void GET(Request req, Response res) {
        String requestedUUID = req.getPath().substring(10);
        try {
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
        } catch (RuntimeException e) {
            Log.i("MYLOG", "FileNotHosted");
            if ("FileNotHosted".equals(e.getMessage())) {
                res.setHeader("Content-Type", "text/html");
                res.setHeader("Content-Disposition", "inline");
                res.sendResponse("File not hosted".getBytes(StandardCharsets.UTF_8));
            } else {
                Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
            }
        }


    }
}
