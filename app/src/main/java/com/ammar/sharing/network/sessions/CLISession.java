package com.ammar.sharing.network.sessions;

import com.ammar.sharing.common.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.SharableApp;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.sessions.base.HTTPSession;

import java.nio.charset.StandardCharsets;

public class CLISession extends HTTPSession {
    public CLISession(String[] paths) {
        super(paths, true);
    }

    @Override
    public void GET(Request req, Response res) {
        String path = req.getPath();
        if ("/ls".equals(path)) {
            listDownloads(req, res);
        } else if (path.startsWith("/dl/")) { // download a file
            String uuid = path.substring(4);
            if (Sharable.sharableUUIDExists(uuid)) {
                sendSharable(uuid, res);
            } else {
                res.setHeader("Content-Type", "text/plain");
                res.setStatusCode(404);
                res.sendResponse("Download doesn't exist.\nMake sure you've typed the URL correctly.\n".getBytes(StandardCharsets.UTF_8));
            }
        } else if ("/da".equals(path)) {
            sendAllSharables(res);
        }
    }


    private void listDownloads(Request req, Response res) {
        res.setHeader("Content-Type", "text/plain");
        StringBuilder listedDownloadsString = new StringBuilder();
        for (Sharable i : Sharable.sharablesList) {
            listedDownloadsString
                    .append("Name: ").append(i.getName()).append("\n")
                    .append("Size: ").append(Utils.getFormattedSize(i.getSize())).append("\n")
                    .append("URL:\n").append("http://").append(req.getHeader("Host")).append("/dl/").append(i.getUUID())
                    .append("\n\n");
        }
        res.sendResponse(listedDownloadsString.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void sendSharable(String uuid, Response res) {
        Sharable file = Sharable.getFileWithUUID(uuid);
        if (!(file instanceof SharableApp)) {
            res.sendFileResponse(file, user);
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
                res.sendZippedFilesResponse(app_files, app.getName() + "apks", user);
            } else {
                res.sendFileResponse(app, user);
            }
        }Sharable.sharablesList.toArray(new Sharable[0]);
    }

    private void sendAllSharables(Response res) {
        Sharable[] sharables = Sharable.sharablesList.toArray(new Sharable[0]);
        res.sendZippedFilesResponse(sharables, "files.zip", user);
    }
}
