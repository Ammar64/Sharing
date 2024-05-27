package com.ammar.filescenter.services.components;

import android.util.Log;

import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.objects.Downloadable;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;

public class ClientHandler implements Runnable {
    private final LinkedList<Downloadable> downloadablesList;
    /**
     * @noinspection FieldCanBeLocal
     */
    private final int timeout = 5000;
    private final Socket clientSocket;

    public ClientHandler(LinkedList<Downloadable> downloadablesList, Socket clientSocket) {
        this.downloadablesList = downloadablesList;
        this.clientSocket = clientSocket;
    }

    // handle client here
    @Override
    public void run() {

        try {
            clientSocket.setKeepAlive(true);
            clientSocket.setSoTimeout(timeout);
        } catch (SocketException e) {
            Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
        }


        try {
            Request request = new Request(clientSocket);
            while (request.readSocket()) {
                String path = request.getPath();
                Response response = new Response(clientSocket);
                // multiply timeout by 0.001 to convert from milliseconds into seconds
                response.setHeader("Keep-Alive", String.format(Locale.ENGLISH, "timeout=%d", (int) (timeout * 0.001)));
                if ("POST".equals(request.getMethod())) {
                    if ("/upload".equals(request.getPath())) {
                        if (request.POST_StoreFile("file"))
                            response.sendResponse("Uploaded successfully".getBytes());
                        else
                            response.sendResponse("Upload Failed".getBytes());

                    }
                } else if ("GET".equals(request.getMethod())) {
                    if ("/available-downloads".equals(path)) {
                        try {
                            byte[] downloadables_response = getDownloadablesJson();
                            response.sendResponse(downloadables_response);
                        } catch (JSONException e) {
                            Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
                        }
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
                            Downloadable downloadable = getDownloadableWithUUID(requestedUUID);
                            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", downloadable.getName()));
                            response.setHeader("Content-Type", "application/octet-stream");
                            response.sendFileResponse(downloadable.getPath());
                        } catch (RuntimeException e) {
                            Log.i("MYLOG", "FileNotHosted");
                            response.setHeader("Content-Type", "text/html");
                            response.setHeader("Content-Disposition", "inline");
                            response.sendResponse("File not hosted".getBytes(StandardCharsets.UTF_8));
                        }
                    } else if ("/favicon.ico".equals(path)) {
                        response.setHeader("Content-Type", "image/svg+xml");
                        response.sendResponse(NetworkService.readFileFromAssets("icons8-share.svg"));
                    } else {
                        response.setStatusCode(404);
                        response.setHeader("Content-Type", "text/html");
                        response.sendResponse("404 What are you doing ?".getBytes(StandardCharsets.UTF_8));
                    }
                }

            }
        } catch (IOException ignored) {

        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
            }
        }

    }

    private Downloadable getDownloadableWithUUID(String uuid) throws RuntimeException {
        for (Downloadable i : downloadablesList) {
            if (uuid.equals(i.getUUID().toString())) {
                return i;
            }
        }
        throw new RuntimeException("FileNotHosted");
    }

    private byte[] getDownloadablesJson() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Downloadable i : downloadablesList) {
            jsonArray.put(i.getJSONObject());
        }
        return jsonArray.toString().getBytes();
    }

}