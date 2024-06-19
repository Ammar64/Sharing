package com.ammar.filescenter.services.components;

import android.util.Log;

import com.ammar.filescenter.custom.io.ProgressManager;
import com.ammar.filescenter.custom.io.ProgressOutputStream;
import com.ammar.filescenter.services.models.Transferable;
import com.ammar.filescenter.services.models.User;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Response {
    private Socket clientSocket;

    public Response(Socket clientSocket) {
        this.clientSocket = clientSocket;
        statusCode = 200;
    }

    public void sendFileResponse(Transferable file) {
        User user = User.getUserBySockAddr(clientSocket.getRemoteSocketAddress());
        ProgressManager progressManager = new ProgressManager(file.getFile(), file.getSize(), user, ProgressManager.OP.DOWNLOAD);
        try {
            ProgressOutputStream out = new ProgressOutputStream(clientSocket.getOutputStream(), progressManager);

            long size = file.getSize();

            setHeader("Content-Length", String.valueOf(size));
            setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFileName()));
            setHeader("Content-Type", file.getMimeType());

            writeHeaders(out);
            try (FileInputStream input = new FileInputStream(file.getFilePath())) {
                byte[] buffer = new byte[2048];
                int bytesRead;

                while ((bytesRead = input.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            progressManager.reportCompleted();

        } catch (IOException e) {
            if( "Broken pipe".equals(e.getMessage()) ) {
                progressManager.reportPaused();
            } else {
                progressManager.reportFailed();
            }
            Log.e("MYLOG", "SendFileResponse(). Exception: " + e.getMessage());
        }
    }


    public void sendZippedFilesResponse(Transferable[] files) {
        User user = User.getUserBySockAddr(clientSocket.getRemoteSocketAddress());
        ProgressManager progressManager = new ProgressManager(files[0].getFile(), -1, user, ProgressManager.OP.DOWNLOAD);
        progressManager.setDisplayName(files[0].getName());
        try {
            OutputStream out = clientSocket.getOutputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
            ZipOutputStream zout = new ZipOutputStream(bout);

            setHeader("Content-Type", "application/zip");
            setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", files[0].getName() + ".zip"));
            setHeader("Transfer-Encoding", "chunked");
            writeHeaders(out);

            zout.setMethod(ZipOutputStream.DEFLATED);
            for (Transferable i : files) {
                ZipEntry zipEntry = new ZipEntry(i.getFileName());
                zout.putNextEntry(zipEntry);

                FileInputStream fin = new FileInputStream(i.getFilePath());
                byte[] buffer = new byte[2048];
                int bytesRead;
                while ((bytesRead = fin.read(buffer)) != -1) {
                    zout.write(buffer, 0, bytesRead);
                    byte[] buf = bout.toByteArray();
                    // write chunk to client socket
                    if( buf.length != 0 ) {
                        out.write(String.format("%x\r\n", buf.length).getBytes());
                        out.write(buf);
                        out.write("\r\n".getBytes());
                    }
                    Log.d("HTTP", String.format("%x\\r\\n", buf.length) + new String(buf) + "\\r\\n" );
                    bout.reset();
                    progressManager.accumulateLoaded(bytesRead);
                }
                fin.close();
                zout.closeEntry();
            }

            // write the end of the zip file and the last chunk
            zout.finish();
            out.write(String.format("%X\r\n", bout.size()).getBytes());
            out.write(bout.toByteArray());
            out.write("\r\n0\r\n\r\n".getBytes());

            progressManager.reportCompleted();

        } catch (IOException e) {
            progressManager.reportFailed();
            Log.e("MYLOG", "sendZippedFilesResponse(). Exception: " + e.getMessage());
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

    public void sendResponse() {
        try {
            OutputStream out = clientSocket.getOutputStream();
            writeHeaders(out);
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

    private Map<String, String> headers = new TreeMap<>();


}
