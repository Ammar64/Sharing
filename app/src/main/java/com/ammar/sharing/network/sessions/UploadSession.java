package com.ammar.sharing.network.sessions;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.custom.io.ProgressManager;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.sessions.base.HTTPSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UploadSession extends HTTPSession {
    public UploadSession(String[] paths) {
        super(paths, true);
    }

    @Override
    public void POST(Request req, Response res) {
        boolean uploadDisabled = Utils.getSettings().getBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, false);

        try {
            String path = req.getPath();
            if ("/check-upload-allowed".equals(path)) {
                JSONObject uploadAllowedJson = new JSONObject();
                uploadAllowedJson.put("allowed", !uploadDisabled);
                res.sendResponse(uploadAllowedJson.toString().getBytes());
            } else if (path.startsWith("/upload/")) {
                if (!uploadDisabled) {
                    String fileName = URLDecoder.decode(path.substring(8), "UTF-8");
                    if (fileName.contains("/")) {
                        res.setStatusCode(400);
                    } else {
                        long content_length = Long.parseLong(req.getHeader("Content-Length"));
                        int status_code;
                        if (req.getHeader("Content-Range") == null)
                            status_code = StoreFile(req, fileName, content_length);
                        else status_code = 501; // not implemented
                        res.setStatusCode(status_code);
                    }
                    res.sendResponse();
                    req.getClientSocket().close();

                } else {
                    res.setStatusCode(423); // locked
                    res.sendResponse();
                }

            }
        } catch (JSONException e) {
            Utils.showErrorDialog("UploadSession.POST(). JSONException", e.getMessage());
            res.setStatusCode(400);
            res.sendResponse();
        } catch (UnsupportedEncodingException e) {
            Utils.showErrorDialog("UploadSession.POST(). UnsupportedEncodingException", e.getMessage());
            res.setStatusCode(400);
            res.sendResponse();
        } catch (IOException e) {
            Utils.showErrorDialog("ClientHandler.POST. IOException: ", e.getMessage());
        }
    }

    private int StoreFile(Request request, String fullFileName, long size) throws IOException {
        InputStream in = request.getClientInput();
        File upload_dir = Utils.getUploadDir(fullFileName);
            if (!upload_dir.exists()) {
                Utils.createAppDirs();
            }
            File upload_file = Utils.createNewFile(upload_dir, fullFileName);
            ProgressManager progressManager = new ProgressManager(upload_file, request.getClientSocket(), size, user, ProgressManager.OP.UPLOAD);
            progressManager.setDisplayName(upload_file.getName());
        try {
            FileOutputStream out = new FileOutputStream(upload_file);
            long totalBytesRead = 0;
            byte[] buffer = new byte[8192];
            while (totalBytesRead < size) {
                int bytesRead = in.read(buffer);
                totalBytesRead += bytesRead;
                if (bytesRead != -1) {
                    progressManager.setLoaded(totalBytesRead);
                    out.write(buffer, 0, bytesRead);
                } else progressManager.reportStopped();
            }
            out.close();
            progressManager.reportCompleted();

            return 200;
        } catch (IOException e) {
            Utils.showErrorDialog("ClientHandler.StoreFile. IOException: ", e.getMessage());
            progressManager.reportStopped();
            try {
                request.getClientSocket().close();
            } catch (IOException ex) {
                Utils.showErrorDialog("ClientHandler.StoreFile. IOException thrown by socket close: ", ex.getMessage());
            }
            return 500;
        }
    }

}
