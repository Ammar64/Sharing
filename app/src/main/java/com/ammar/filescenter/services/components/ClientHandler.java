package com.ammar.filescenter.services.components;

import android.util.Log;

import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.models.Upload;
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
                        response.setHeader("Content-Type", "text/plain");
                        if (request.POST_StoreFile("file"))
                            response.sendResponse("Uploaded successfully".getBytes());
                        else
                            response.sendResponse("Upload Failed".getBytes());

                    }
                } else if ("GET".equals(request.getMethod())) {
                    if ("/available-downloads".equals(path)) {
                        try {
                            byte[] downloadables_response = getFileJson();
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
                            Upload file = getFileWithUUID(requestedUUID);
                            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFileName()));
                            response.setHeader("Content-Type", file.getMimeType());
                            response.sendFileResponse(file);
                        } catch (RuntimeException e) {
                            Log.i("MYLOG", "FileNotHosted");
                            if( "FileNotHosted".equals(e.getMessage()) ) {
                                response.setHeader("Content-Type", "text/html");
                                response.setHeader("Content-Disposition", "inline");
                                response.sendResponse("File not hosted".getBytes(StandardCharsets.UTF_8));
                            } else {
                                Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    } else if ("/favicon.ico".equals(path)) {
                        response.setHeader("Content-Type", "image/svg+xml");
                        response.sendResponse(NetworkService.readFileFromAssets("icons8-share.svg"));
                    } else if ("/dv.png".equals(path)) {
                        response.setHeader("Content-Type", "image/png");
                        response.sendResponse(NetworkService.readFileFromAssets("dv.png"));
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

    private Upload getFileWithUUID(String uuid) throws RuntimeException {
        for (Upload i : Server.filesList) {
            if (uuid.equals(i.getUUID())) {
                return i;
            }
        }
        throw new RuntimeException("FileNotHosted");
    }

    private byte[] getFileJson() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Upload i : Server.filesList) {
            jsonArray.put(i.getJSON());
        }
        return jsonArray.toString().getBytes();
    }

}