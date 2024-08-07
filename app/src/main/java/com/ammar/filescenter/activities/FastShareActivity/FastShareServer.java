package com.ammar.filescenter.activities.FastShareActivity;

import static com.ammar.filescenter.common.Utils.readLineUTF8;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.ammar.filescenter.common.FileUtils;
import com.ammar.filescenter.common.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Locale;
import java.util.StringTokenizer;

public class FastShareServer {
    private final ServerSocket serverSocket;
    private final FastShareActivity act;
    private final Uri fileUri;
    public static final int FAST_SHARE_PORT = 3000;

    public FastShareServer(FastShareActivity act, Uri uri) {
        try {
            serverSocket = new ServerSocket(FAST_SHARE_PORT);
            this.act = act;
            fileUri = uri;
            new Thread(this::serve).start();
        } catch (IOException e) {
            Utils.showErrorDialog("FastShareServer.<init>. IOException:", e.getMessage());
            throw new RuntimeException("Error Opening socket");
        }
    }

    Socket socket = null;
    private void serve() {
        try {
            for (; ; ) {
                socket = serverSocket.accept();
                if( !isValidRequest(socket.getInputStream()) ) {
                    socket.close();
                    continue;
                };
                AssetFileDescriptor fileDescriptor = act.getContentResolver().openAssetFileDescriptor(fileUri, "r");
                if (fileDescriptor != null) {
                    long fileSize = fileDescriptor.getLength();
                    InputStream input = fileDescriptor.createInputStream();
                    OutputStream output = socket.getOutputStream();

                    writeHeader(output, FileUtils.getFileName(act.getContentResolver(), fileUri), fileSize);

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }


                    input.close();
                    fileDescriptor.close();
                }
                socket.close();
            }
        } catch (SocketException ignored) {
        } catch (IOException e) {
            Utils.showErrorDialog("FastShareServer.serve. IOException:", e.getMessage());
        }

    }

    private boolean isValidRequest(InputStream inputStream) throws IOException {
        String line = readLineUTF8(inputStream);
        StringTokenizer st = new StringTokenizer(line);
        if (line.isEmpty()) {
            return false;
        }

        if (!st.hasMoreTokens()) {
            return false;
        }

        String method = st.nextToken();
        if(!"GET".equalsIgnoreCase(method)) return false;
        if (!st.hasMoreTokens()) {
            return false;
        }

        String uri = st.nextToken();
        if( !uri.equals("/") ) return false;

        return true;
    }

    public void stopServing() {
        try {
            serverSocket.close();
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Utils.showErrorDialog("FastShareServer.stopServing(). IOException:", e.getMessage());
        }
    }


    private void writeHeader(OutputStream output, String filename, long fileSize) throws IOException {
        String mimeType = Utils.getMimeType(filename, true);
        if (mimeType.equals("*/*")) mimeType = "application/octet-stream";

        output.write(String.format(
                Locale.ENGLISH,
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: %s\r\n" +
                        "Content-Length: %d\r\n" +
                        "Content-Disposition: attachment; filename=\"%s\"\r\n" +
                        "Accept-Ranges: none\r\n\r\n",
                mimeType,
                fileSize,
                filename
        ).getBytes());
    }
}
