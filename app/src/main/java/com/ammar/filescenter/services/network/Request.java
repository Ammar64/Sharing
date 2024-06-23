package com.ammar.filescenter.services.network;

import static com.ammar.filescenter.common.Utils.readLineUTF8;

import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.network.exceptions.BadRequestException;
import com.ammar.filescenter.services.network.exceptions.NotImplementedException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class Request {
    private final Socket clientSocket;
    private final BufferedInputStream clientInput;

    public Request(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;

            clientInput = new BufferedInputStream(clientSocket.getInputStream());

        } catch (IOException e) {
            throw new RuntimeException("IOException in Request constructor");
        }
        this.headers = new HashMap<>();
    }

    public boolean readSocket() {
        if (_connClose) return false;
        try {
            params = new TreeMap<>();
            parseHTTPHeader(clientInput);

            String connection = headers.get("Connection");
            if (connection != null && connection.contains("keep-alive")) {
                _connClose = true;
            } else {
                try {
                    clientSocket.setKeepAlive(true);
                    clientSocket.setSoTimeout(ClientHandler.timeout);
                } catch (SocketException e) {
                    Utils.showErrorDialog("Request.readSocket(). SocketException:", e.getMessage());
                }
            }
            return true;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (NullPointerException e) {
            Utils.showErrorDialog("Request.readSocket(). NullPointerException:", e.getMessage());
            return false;
        } catch (BadRequestException e) {
            Utils.showErrorDialog("Request.readSocket. BadRequestException:", e.getMessage());
            return false;
        } catch (Exception e) {
            Utils.showErrorDialog("Request.readSocket. Exception:", e.getMessage());
            return false;
        }
    }

    private void parseHTTPHeader(InputStream in) throws IOException, BadRequestException, NotImplementedException {

        String line = readLineUTF8(clientInput);
        StringTokenizer st = new StringTokenizer(line);
        if (line.isEmpty()) {
            return;
        }

        if (!st.hasMoreTokens()) {
            throw new RuntimeException("BAD REQUEST: Syntax error. Usage: GET /example/file.html");
        }

        this.method = st.nextToken();

        if (!st.hasMoreTokens()) {
            throw new RuntimeException("BAD REQUEST: Missing URI. Usage: GET /example/file.html");
        }

        String uri = st.nextToken();
        int i = uri.indexOf('?');
        if (i >= 0) {
            decodeParams(uri.substring(i + 1));
            this.path = URLDecoder.decode(uri.substring(0, i), "UTF-8");
        } else {
            this.path = URLDecoder.decode(uri, "UTF-8");
        }

        this.version = "HTTP/1.1";
        while ((!(line = readLineUTF8(clientInput)).isEmpty())) {
            String[] headerParts = line.split(": ");
            if (headerParts.length != 2) throw new RuntimeException("Header is invalid");
            this.headers.put(headerParts[0], headerParts[1]);
        }

        String contentLengthT = getHeader("Content-Length");
        if (contentLengthT == null) content_length = -1;
        else content_length = Long.parseLong(contentLengthT);

        if ("POST".equals(this.method)) {
            if ("application/x-www-form-urlencoded".equals(getHeader("Content-Type"))) {
                if (content_length <= 4096) {
                    byte[] buff = new byte[(int) content_length];
                    decodeParams(new String(buff));
                } else
                    throw new BadRequestException("Max POST request with application/x-www-form-urlencoded is 4MB");
            } else if ("multipart/form-data".equals(getHeader("Content-Type"))) {
                // no need to implement to hurt your head
                throw new NotImplementedException("Server didn't implement multipart/form-data");
            }
        }
    }

    private void decodeParams(String params) throws BadRequestException {
        String[] fields = params.split("&");
        for (String i : fields) {
            StringTokenizer st = new StringTokenizer(i);
            String key = st.nextToken("=");

            // if string doesn't contain a value throw error.
            if (!st.hasMoreElements()) throw new BadRequestException("param only contains a key");
            String val = st.nextToken("=").substring(1);

            if (st.hasMoreElements())
                throw new BadRequestException("param contains more than a value");

            try {
                this.params.put(
                        URLDecoder.decode(key, "UTF-8"),
                        URLDecoder.decode(val, "UTF-8")
                );
            } catch (UnsupportedEncodingException e) {
                throw new BadRequestException("UnsupportedEncodingException: " + e.getMessage());
            }

        }
    }

    public String getBody() throws BadRequestException {
        if ("application/json".equals(getHeader("Content-Type"))) {
            if (content_length <= 4096) {
                byte[] buff = new byte[(int) content_length];
                return new String(buff);
            } else
                throw new BadRequestException("Max POST request is 4MB unless it's a file upload");
        }
        throw new RuntimeException("Request.getBody() called with unsupported Content-Type. Supported type are application/json for now");
    }

    public long getStartRange() {
        String range = getHeader("Range");

        if (range == null) return -1;
        StringTokenizer st = new StringTokenizer(range);
        if (!st.hasMoreTokens() || !"bytes".equals(st.nextToken("="))) return -1;
        String bytesStart = st.nextToken("-").replaceAll("=", "");
        return Long.parseLong(bytesStart);
    }

    private String method;
    private String path;
    private String version;
    private final Map<String, String> headers;
    private Map<String, String> params;
    private long content_length;

    /**
     * @noinspection unused
     */
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public long getBodySize() {
        return content_length;
    }

    /**
     * @noinspection unused
     */
    public Map<String, String> getParams() {
        return params;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public BufferedInputStream getClientInput() {
        return clientInput;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public String getParam(String key) {
        return params.get(key);
    }

    private boolean _connClose = false;

    public boolean isKeepAlive() {
        return !_connClose;
    }

}
