package com.ammar.filescenter.services.components;

import android.util.Log;

import com.ammar.filescenter.custom.io.ProgressManager;
import com.ammar.filescenter.custom.io.ProgressOutputStream;
import com.ammar.filescenter.services.models.Upload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Response {
    private Socket clientSocket;

    public Response(Socket clientSocket) {
        this.clientSocket = clientSocket;
        statusCode = 200;
    }

    public void sendFileResponse(Upload file) {
        try {

            ProgressManager progressManager = new ProgressManager(file.getFileName(), file.getSize(), clientSocket.getRemoteSocketAddress(), ProgressManager.OP.DOWNLOAD);
            ProgressOutputStream out = new ProgressOutputStream(clientSocket.getOutputStream(), progressManager);

            long size = file.getSize();
            setHeader("Content-Length", String.valueOf(size));

            writeHeaders(out);
            try (FileInputStream input = new FileInputStream(file.getFilePath())) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            progressManager.reportCompleted();

        } catch (IOException e) {
            Log.e("MYLOG", "", e);
        }
    }

    public void sendResponse(byte[] content) {
        try {
            OutputStream out = clientSocket.getOutputStream();
            setHeader("Content-Length", String.valueOf(content.length));
            writeHeaders(out);
            out.write(content);
            out.flush();

        } catch (IOException e) {
            Log.e("MYLOG", e.getMessage());
        }
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }


    private void writeTopHeader(OutputStream out, int statusCode) throws IOException {
        String readableStatus = "";
        switch (statusCode) {
            case 200:
                readableStatus = "OK";
                break;
            case 404:
                readableStatus = "Not found";
                break;
        }
        byte[] buffer = String.format(Locale.ENGLISH, "HTTP/1.1 %d %s\r\n", statusCode, readableStatus).getBytes();
        out.write(buffer);
    }

    private void writeHeaders(OutputStream out) throws IOException {
        writeTopHeader(out, statusCode);
        for (Map.Entry<String, String> i : headers.entrySet()) {
            byte[] header = String.format(Locale.ENGLISH, "%s: %s\r\n", i.getKey(), i.getValue()).getBytes();
            out.write(header);
        }
        out.write("\r\n".getBytes());
    }


    private int statusCode;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    private Map<String, String> headers = new HashMap<>();


}
