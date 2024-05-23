package com.ammar.filescenter.services.components;

import android.util.Log;

import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.objects.Downloadable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Locale;

public class ClientHandler implements Runnable {
    LinkedList<Downloadable> downloadablesList;
    private final int timeout = 5000;
    private final Socket clientSocket;

    public ClientHandler(LinkedList<Downloadable> downloadablesList, Socket clientSocket) {
        this.downloadablesList = downloadablesList;
        this.clientSocket = clientSocket;
    }

    // handle client here
    @Override
    public void run() {

        // use this variable later
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();

        try {
            clientSocket.setKeepAlive(true);
            clientSocket.setSoTimeout(timeout);
        } catch (SocketException e) {
            Log.e("MYLOG", e.getMessage());
        }

        Request request = new Request(clientSocket);
        while(request.readSocket()) {
            String path = request.getPath();
            Response response = new Response(clientSocket);
            // multiply timeout by 0.001 to convert from milliseconds into seconds
            response.setHeader("Keep-Alive", String.format(Locale.ENGLISH, "timeout=%d", (int)(timeout * 0.001)));

            if ("available-downloads".equals(path)) {
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (Downloadable i : downloadablesList) {
                        JSONObject downloadableObject = new JSONObject();
                        downloadableObject.accumulate("uuid", i.getUUID());
                        downloadableObject.accumulate("name", i.getName());
                        downloadableObject.accumulate("size", i.getSize());
                        jsonArray.put(downloadableObject);
                    }
                    Log.d("MYLOG", jsonArray.toString(4));
                } catch (JSONException e) {
                    Log.e("MYLOG", e.getMessage());
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
            } else {
                response.setStatusCode(404);
                response.setHeader("Content-Type", "text/html");
                response.sendResponse("404 What are you doing ?".getBytes(StandardCharsets.UTF_8));
            }
        }

    }

}