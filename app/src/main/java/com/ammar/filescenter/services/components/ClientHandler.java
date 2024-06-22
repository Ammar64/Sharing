package com.ammar.filescenter.services.components;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.ammar.filescenter.activities.MainActivity.fragments.SettingsFragment;
import com.ammar.filescenter.common.Abbrev;
import com.ammar.filescenter.custom.io.ProgressManager;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.models.TransferableApp;
import com.ammar.filescenter.services.models.Transferable;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;


public class ClientHandler implements Runnable {
    /**
     * @noinspection FieldCanBeLocal
     */
    public static final int timeout = 5000;
    private final Socket clientSocket;

    private final Context context;
    private final SharedPreferences settings;

    public ClientHandler(Context context, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.context = context;
        this.settings = this.context.getSharedPreferences(SettingsFragment.SettingsPrefFile, Context.MODE_PRIVATE);
    }

    // handle client here
    @Override
    public void run() {

        try {
            Request request = new Request(clientSocket);
            while (request.readSocket()) {


                String path = request.getPath();
                Response response = new Response(clientSocket);
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                // multiply timeout by 0.001 to convert from milliseconds into seconds
                response.setHeader("Keep-Alive", request.isKeepAlive() ? String.format(Locale.ENGLISH, "timeout=%d", (int) (timeout * 0.001)) : "close");

                String userAgent = request.getHeader("User-Agent");
                User user = null;
                if (userAgent != null)
                    user = User.RegisterUser(settings, clientSocket.getRemoteSocketAddress(), userAgent);
                if (user != null && !user.isBlocked()) {
                    if ("GET".equals(request.getMethod())) {
                        if ("/available-downloads".equals(path)) {
                            byte[] available = getFileJson();
                            response.sendResponse(available);
                        } else if ("/".equals(path) || "/index.html".equals(path)) {
                            response.setHeader("Content-Type", "text/html");
                            response.sendResponse(NetworkService.readFileFromAssets("index.html"));
                        } else if ("/style.css".equals(path)) {
                            response.setHeader("Content-Type", "text/css");
                            response.sendResponse(NetworkService.readFileFromAssets("style.css"));
                        } else if ("/script.js".equals(path)) {
                            response.setHeader("Content-Type", "text/javascript");
                            response.sendResponse(NetworkService.readFileFromAssets("script.js"));
                        } else if (path.startsWith("/download/")) {
                            String requestedUUID = path.substring(10);
                            try {
                                Transferable file = getFileWithUUID(requestedUUID);
                                if (!(file instanceof TransferableApp)) {
                                    long start = getStartRange(request);
                                    if (start == -1)
                                        response.sendFileResponse(file, user);
                                    else response.resumePausedFileResponse(file, start, user);
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
                                        response.sendZippedFilesResponse(app_files, user);
                                    } else {
                                        long start = getStartRange(request);
                                        if (start == -1)
                                            response.sendFileResponse(app, user);
                                        else response.resumePausedFileResponse(app, start, user);
                                    }
                                }
                            } catch (RuntimeException e) {
                                Log.i("MYLOG", "FileNotHosted");
                                if ("FileNotHosted".equals(e.getMessage())) {
                                    response.setHeader("Content-Type", "text/html");
                                    response.setHeader("Content-Disposition", "inline");
                                    response.sendResponse("File not hosted".getBytes(StandardCharsets.UTF_8));
                                } else {
                                    Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
                                }
                            }

                        } else if(path.startsWith("/get-icon/")) {
                            String uuid = path.substring(10);
                            Transferable file = getFileWithUUID(uuid);
                            if( file instanceof TransferableApp ) {
                                TransferableApp app = (TransferableApp) file;
                                Bitmap appIconBM = Utils.drawableToBitmap(app.getIcon());
                                response.sendBitmapResponse(appIconBM);
                            }
                        } else if ("/favicon.ico".equals(path)) {
                            response.setHeader("Content-Type", "image/svg+xml");
                            response.sendResponse(NetworkService.readFileFromAssets("icons8-share.svg"));
                        } else if ("/dv.png".equals(path)) {
                            response.setHeader("Content-Type", "image/png");
                            response.sendResponse(NetworkService.readFileFromAssets("dv.png"));
                        } else if ("/get-user-info".equals(path)) {
                            JSONObject userJson = new JSONObject();
                            userJson.put("id", user.getId());
                            response.setStatusCode(200);
                            response.sendResponse(userJson.toString().getBytes());
                        } else if ("/blocked".equals(path)) { // user is not blocked but request blocked page
                            response.setStatusCode(307);
                            response.setHeader("Location", "/");
                            response.sendResponse();
                        } else {
                            response.setStatusCode(404);
                            response.setHeader("Content-Type", "text/html");
                            response.sendResponse("404 What are you doing ?".getBytes(StandardCharsets.UTF_8));
                        }
                    } else if ("PUT".equals(request.getMethod())) {
                        boolean uploadDisabled = settings.getBoolean(SettingsFragment.UploadDisable, false);
                        if ("/check-upload-allowed".equals(path)) {
                            JSONObject uploadAllowedJson = new JSONObject();
                            uploadAllowedJson.put("allowed", !uploadDisabled);
                            response.sendResponse(uploadAllowedJson.toString().getBytes());
                        } else if (!uploadDisabled) {
                            if (path.startsWith("/upload/")) {

                                String fileName = URLDecoder.decode(path.substring(8), "UTF-8");
                                if (fileName.contains("/")) {
                                    response.setStatusCode(400);
                                } else {
                                    long content_length = Long.parseLong(request.getHeader("Content-Length"));
                                    int status_code;
                                    if (request.getHeader("Content-Range") == null)
                                        status_code = StoreFile(request.getClientInput(), fileName, content_length);
                                    else status_code = 501; // not implemented
                                    response.setStatusCode(status_code);
                                }
                                response.sendResponse();
                            }
                        } else {
                            response.setStatusCode(423); // Locked
                            response.sendResponse();
                        }
                    } else if ("POST".equals(request.getMethod())) {
                    }
                    // if user is blocked redirect to blocked page
                } else showBlockedPage(request, response);
            }


        } catch (IOException e) {
            Log.e("MYLOG", "ClientHandler.run(). IOException: " + e.getMessage());
        } catch (JSONException e) {
            Log.e("MYLOG", "ClientHandler.run(). JSONException: " + e.getMessage());
        } catch (Exception e){

        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e("MYLOG", "ClientHandler.run().finally IOException: " + e.getMessage());
            }
        }
    }

    private Transferable getFileWithUUID(String uuid) throws RuntimeException {
        for (Transferable i : Server.filesList) {
            if (uuid.equals(i.getUUID())) {
                return i;
            }
        }
        throw new RuntimeException("FileNotHosted");
    }

    private byte[] getFileJson() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Transferable i : Server.filesList) {
            jsonArray.put(i.getJSON());
        }
        return jsonArray.toString().getBytes();
    }

    private int StoreFile(InputStream in, String fullFileName, long size) {
        File upload_dir = getUploadDir(fullFileName);
        if (!upload_dir.exists()) {
            Utils.createAppDirs();
        }
        File upload_file = Utils.createNewFile(upload_dir, fullFileName);
        User user = User.getUserBySockAddr(clientSocket.getRemoteSocketAddress());
        ProgressManager progressManager = new ProgressManager(upload_file, size, user, ProgressManager.OP.UPLOAD);
        progressManager.setDisplayName(upload_file.getName());
        try {
            FileOutputStream out = new FileOutputStream(upload_file);
            long totalBytesRead = 0;
            byte[] buffer = new byte[8192];
            while (totalBytesRead < size) {
                int bytesRead = in.read(buffer);
                totalBytesRead += bytesRead;
                if(bytesRead != -1) {
                    progressManager.setLoaded(totalBytesRead);
                    out.write(buffer, 0, bytesRead);
                } else progressManager.reportStopped();
            }
            out.close();
            progressManager.reportCompleted();
            return 200;
        } catch (IOException e) {
            Log.e("MYLOG", "ClientHandler.StoreFile. IOException: " + e.getMessage());
            progressManager.reportStopped();
            return 500;
        }
    }


    private void showBlockedPage(Request req, Response res) throws IOException {
        if (!"/blocked".equals(req.getPath())) {
            res.setStatusCode(307);
            res.setHeader("Location", "/blocked");
            res.sendResponse();
        } else {
            res.setStatusCode(401);
            res.setHeader("Content-Type", "text/html");
            res.sendResponse(NetworkService.readFileFromAssets("blocked.html"));
            clientSocket.close();
        }
    }

    private File getUploadDir(String fileName) {
        String mimeType = Utils.getMimeType(fileName);
        if (mimeType.startsWith("image/")) {
            return Abbrev.imagesDir;
        } else if (mimeType.equals("application/vnd.android.package-archive")) {
            return Abbrev.appsDir;
        } else if (mimeType.startsWith("video/")) {
            return Abbrev.videosDir;
        } else if (mimeType.startsWith("audio/")) {
            return Abbrev.audioDir;
        } else if (Utils.isDocumentType(mimeType)) {
            return Abbrev.documentsDir;
        } else {
            Log.d("MYLOG", "Type: " + mimeType);
            return Abbrev.filesDir;
        }
    }

    private long getStartRange(Request req) {
        String range = req.getHeader("Range");

        if (range == null) return -1;
        StringTokenizer st = new StringTokenizer(range);
        if (!st.hasMoreTokens() || !"bytes".equals(st.nextToken("="))) return -1;
        String bytesStart = st.nextToken("-").replaceAll("=", "");
        return Long.parseLong(bytesStart);
    }
}
