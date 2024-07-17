package com.ammar.filescenter.network;

import android.graphics.Bitmap;
import android.util.Log;

import com.ammar.filescenter.custom.io.ProgressManager;
import com.ammar.filescenter.custom.io.ProgressOutputStream;
import com.ammar.filescenter.models.Sharable;
import com.ammar.filescenter.models.User;

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

    public void sendFileResponse(Sharable file, User user) {
        sendFileResponse(file, true, user);
    }


    public void sendFileResponse(Sharable file, boolean progress, User user) {
        ProgressManager progressManager = null;
        if( progress ) {
            progressManager = new ProgressManager(file.getFile(), file.getSize(), user, ProgressManager.OP.DOWNLOAD);
            progressManager.setDisplayName(file.getName());
            progressManager.setUUID(file.getUUID());
        }

        try {
            OutputStream out;

            if( progress )
                out = new ProgressOutputStream(clientSocket.getOutputStream(), progressManager);
            else
                out = clientSocket.getOutputStream();

            long size = file.getSize();

            setHeader("Content-Length", String.valueOf(size));
            setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFileName()));
            setHeader("Content-Type", file.getMimeType());
            setHeader("Accept-Ranges", "bytes");
            writeHeaders(out);
            try (FileInputStream input = new FileInputStream(file.getFilePath())) {
                int bytesRead;
                byte[] buffer = new byte[2048];
                while ((bytesRead = input.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

            if( progress )
                progressManager.reportCompleted();

        } catch (IOException e) {
            if(progress)
                progressManager.reportStopped();
            Log.e("MYLOG", "SendFileResponse(). Exception: " + e.getMessage());
        }
    }

    public void resumePausedFileResponse(Sharable file, long start, User user) {
        // get the stopped progress manager
        ProgressManager progressManager = null;
        for (ProgressManager i : ProgressManager.progresses) {
            if (file.getUUID().equals(i.getUUID())) {
                progressManager = i;
                break;
            }
        }
        try (FileInputStream input = new FileInputStream(file.getFilePath())) {
            long n = input.skip(start);
            if( progressManager == null || n != start ) {
                setStatusCode(400);
                sendResponse();
                return;
            }
            setStatusCode(206);
            setHeader("Content-Length", String.valueOf(file.getSize() - start));
            setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFileName()));
            setHeader("Content-Type", file.getMimeType());
            setHeader("Accept-Ranges", "bytes");
            setHeader("Content-Range", String.format(Locale.ENGLISH, "bytes %d-%d/%d", start, file.getSize()-1, file.getSize()));


            OutputStream out = new ProgressOutputStream(clientSocket.getOutputStream(), progressManager);
            writeHeaders(out);
            progressManager.setLoaded(start);

            int bytesRead;
            byte[] buffer = new byte[2048];
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            progressManager.reportCompleted();
        } catch (IOException e) {
            if( progressManager != null )
                progressManager.reportStopped();
            Log.e("MYLOG", "Response.resumePausedFileResponse. IOException: " + e.getMessage());
        }

    }

    public void sendZippedFilesResponse(Sharable[] files, User user) {
        ProgressManager progressManager = new ProgressManager(files[0].getFile(), -1, user, ProgressManager.OP.DOWNLOAD);
        progressManager.setUUID(files[0].getUUID());
        progressManager.setDisplayName(files[0].getName());
        try {
            OutputStream out = clientSocket.getOutputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
            ZipOutputStream zout = new ZipOutputStream(bout);

            setHeader("Content-Type", "application/octet-stream");
            setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", files[0].getName() + ".apks"));
            setHeader("Transfer-Encoding", "chunked");
            setHeader("Accept-Ranges", "none");

            writeHeaders(out);

            zout.setMethod(ZipOutputStream.DEFLATED);
            for (Sharable i : files) {
                ZipEntry zipEntry = new ZipEntry(i.getFileName());
                zout.putNextEntry(zipEntry);

                FileInputStream fin = new FileInputStream(i.getFilePath());
                byte[] buffer = new byte[2048];
                int bytesRead;
                while ((bytesRead = fin.read(buffer)) != -1) {
                    zout.write(buffer, 0, bytesRead);
                    byte[] buf = bout.toByteArray();
                    // write chunk to client socket
                    if (buf.length != 0) {
                        out.write(String.format("%x\r\n", buf.length).getBytes());
                        out.write(buf);
                        out.write("\r\n".getBytes());
                    }
                    Log.d("HTTP", String.format("%x\\r\\n", buf.length) + new String(buf) + "\\r\\n");
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
            progressManager.reportStopped();
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
            Log.e("MYLOG", "Response.sendResponse(byte[])" + e.getMessage());
        }
    }

    public void sendResponse() {
        try {
            OutputStream out = clientSocket.getOutputStream();
            writeHeaders(out);
            out.flush();
        } catch (IOException e) {
            Log.e("MYLOG", "Response.sendResponse()" + e.getMessage());
        }
    }

    public void sendBitmapResponse(Bitmap bitmap) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer);
        Log.d("MYLOG", "Bitmap buffer size: " + buffer.size());
        setHeader("Content-Type", "image/png");
        sendResponse(buffer.toByteArray());
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
            case 206:
                readableStatus = "Partial Content";
                break;
            case 404:
                readableStatus = "Not found";
                break;
            case 400:
                readableStatus = "Bad request";
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


    public void close() throws IOException {
        clientSocket.close();
    }
}
