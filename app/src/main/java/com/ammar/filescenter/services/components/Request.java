package com.ammar.filescenter.services.components;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Request {
    private Socket clientSocket;
    BufferedReader br;

    public Request(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException("IOException in Request constructor");
        }
        this.headers = new HashMap<>();
    }

    public boolean readSocket() {
        try {

            params = new HashMap<>();

            int lineNumber = 1;
            String line;
            while (!(line = br.readLine()).isEmpty()) {
                handleHTTPHeader(line, lineNumber);
                lineNumber++;
            }
            return true;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (NullPointerException e) {
            // for some reason this is thrown when Pipe is broken
            return false;
        } catch (Exception e) {
            Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
            return false;
        }
    }

    private void handleHTTPHeader(String line, int lineNumber) {
        if (lineNumber < 1)
            throw new RuntimeException("HTTP header line number can't be less than 1");
        if (lineNumber == 1) {
            String[] requestInfo = line.split(" ");

            // parse the method
            // requestInfo[0] is the method
            if (!("GET".equals(requestInfo[0]) || "POST".equals(requestInfo[0]))) {
                throw new RuntimeException("Illegal method");
            } else {
                this.method = requestInfo[0];
            }

            // TODO: validate request path
//            if (!requestInfo[1].matches("^/\\w+((/\\w+)*)^/\\w+((/\\w+)*)?^.*[your_char_here].*$((\\?(\\w+=(?:\\w+|%[0-9a-fA-F]{2})+(?:=%[0-9a-fA-F]{2})*(&\\w+=(?:\\w+|%[0-9a-fA-F]{2})+(?:=%[0-9a-fA-F]{2})*)*)?)|/)?$")) {
//                throw new RuntimeException("Invalid path or query string");
//            }
            String[] pathAndParams = requestInfo[1].split("\\?");

            String path = pathAndParams[0];
            if (!path.startsWith("/") || path.contains("..")) {
                throw new RuntimeException("Illegal Path");
            } else {
                if (path.length() != 1 && path.endsWith("/"))
                    path = path.substring(0, path.length() - 1);
                this.path = path;
            }

            if (pathAndParams.length > 1) {
                String query_string = pathAndParams[1];
                String[] query_parts = query_string.split("&");
                for (String i : query_parts) {
                    String[] param = i.split("=");
                    // check if it's wrong
                    if (param.length != 2)
                        throw new RuntimeException("Parameter has more than key-value");
                    try {
                        this.params.put(URLDecoder.decode(param[0], "UTF-8"), URLDecoder.decode(param[1], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("MYLOG", Objects.requireNonNull(e.getMessage()));
                    }

                }
            }


            this.version = requestInfo[2];

        } else {
            String[] headerParts = line.split(": ");
            if (headerParts.length != 2) throw new RuntimeException("Header is invalid");
            this.headers.put(headerParts[0], headerParts[1]);
        }
    }

    private String method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private Map<String, String> params;

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

}
