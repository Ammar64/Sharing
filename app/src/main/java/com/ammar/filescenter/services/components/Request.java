package com.ammar.filescenter.services.components;

import static com.ammar.filescenter.common.Utils.readLineUTF8;

import android.os.Environment;
import android.util.Log;

import com.ammar.filescenter.custom.io.ProgressManager;
import com.ammar.filescenter.custom.io.ProgressOutputStream;
import com.ammar.filescenter.services.models.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {
    private Socket clientSocket;
    private BufferedInputStream clientInput;
    private int charsRead = 0;

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
        if(_connClose) return false;
        try {
            params = new HashMap<>();
            int lineNumber = 1;
            String line;
            charsRead = 0;
            while ((!(line = readLineUTF8(clientInput)).isEmpty())) {
                charsRead += line.length() + 2; // string length + \r + \n

                handleHTTPHeader(line, lineNumber);
                lineNumber++;
            }
            charsRead += 2; // the \r and \n.

            String connection = headers.get("Connection");
            if( connection != null && connection.contains("keep-alive") ) {
                _connClose = true;
            } else {
                try {
                    clientSocket.setKeepAlive(true);
                    clientSocket.setSoTimeout(ClientHandler.timeout);
                } catch (SocketException e) {
                    Log.e("MYLOG", "readSocket(): " + Objects.requireNonNull(e.getMessage()));
                }
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
            if (!("GET".equals(requestInfo[0]) || "POST".equals(requestInfo[0]) || "PUT".equals(requestInfo[0]))) {
                throw new RuntimeException("Illegal method");
            }
            this.method = requestInfo[0];
            this.version = requestInfo[2];

            // TODO: validate request path
//            if (!requestInfo[1].matches("^/\\w+((/\\w+)*)^/\\w+((/\\w+)*)?^.*[your_char_here].*$((\\?(\\w+=(?:\\w+|%[0-9a-fA-F]{2})+(?:=%[0-9a-fA-F]{2})*(&\\w+=(?:\\w+|%[0-9a-fA-F]{2})+(?:=%[0-9a-fA-F]{2})*)*)?)|/)?$")) {
//                throw new RuntimeException("Invalid path or query string");
//            }
            String[] pathAndParams = requestInfo[1].split("\\?");

            // check if POST request is invalid
            if ("POST".equals(this.method) && pathAndParams.length > 1) {
                throw new RuntimeException("Invalid POST request");
            }

            String path = pathAndParams[0];
            if (!path.startsWith("/") || path.contains("..")) {
                throw new RuntimeException("Illegal Path");
            } else {
                if (path.length() != 1 && path.endsWith("/"))
                    path = path.substring(0, path.length() - 1);
                this.path = path;
            }
            if ("GET".equals(this.method)) {

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


            }
        } else {
            String[] headerParts = line.split(": ");
            if (headerParts.length != 2) throw new RuntimeException("Header is invalid");
            this.headers.put(headerParts[0], headerParts[1]);
        }
    }

    public boolean POST_StoreFile(String paramName) {
        charsRead = 0;
        if (!"POST".equals(this.method)) {
            Log.e("MYLOG", "Call to POST_StoredFile with " + this.method + " request");
            return false;
        }
        String content_type = this.headers.get("Content-Type");
        if (content_type == null) return false;
        String[] content_type_params = content_type.split("; ");
        Map<String, String> content_type_values = new TreeMap<>();
        String boundary = "";
        boolean isMultiPart = false;
        int content_length;
        try {
            content_length = Integer.parseInt(this.headers.get("Content-Length"));
        } catch (NumberFormatException ignore) {
            return false;
        }

        for (String i : content_type_params) {
            String[] key_value = i.split("=");
            if (key_value.length > 1) {
                if ("boundary".equals(key_value[0])) {
                    boundary = key_value[1];
                }
            } else {
                if ("multipart/form-data".equals(key_value[0])) {
                    isMultiPart = true;
                }
            }
        }

        try {
            if (isMultiPart) {


                String name;
                String filename = "";
                boolean readingBody = false;
                // loop through lines

                for (int lineNum = 1; ; lineNum++) {
                    String line = "";
                    if (!readingBody) {
                        line = readLineUTF8(clientInput);
                        charsRead += line.length() + 2; // string length + \r + \n
                    }
                    if (line.equals("--" + boundary)) {
                        lineNum = 1;
                        readingBody = false;
                        continue;
                    }
                    if (lineNum != 1) {
                        if (!readingBody) {
                            if (line.isEmpty()) {
                                readingBody = true;
                                continue;
                            }
                            String[] header = line.split(": ");
                            if ("Content-Disposition".equals(header[0])) {
                                Map<String, String> values = parseAndSplit(header[1]);

                                if (!values.containsKey("form-data")) return false;
                                name = values.get("name");
                                filename = values.get("filename");
                                if (name == null || filename == null) return false;
                                if (!name.equals(paramName)) return false;
                            }
                        } else {
                            File file_upload = new File(Environment.getExternalStorageDirectory(), "Download/" + filename);
                            if (file_upload.exists()) file_upload.delete();
                            if (file_upload.createNewFile()) {

                                long sz = content_length - charsRead - boundary.length() - 8;
                                Log.d("MYLOG", String.format(Locale.ENGLISH, "sz: %d, charsRead: %d, boundary.length: %d", sz, charsRead, boundary.length()));
                                User user = User.getUserBySockAddr(clientSocket.getRemoteSocketAddress());
                                ProgressManager progressManager = new ProgressManager(file_upload, sz, user, ProgressManager.OP.UPLOAD);
                                try {
                                    BufferedOutputStream out = new BufferedOutputStream(new ProgressOutputStream(new FileOutputStream(file_upload), progressManager));


                                    // boundary index
                                    int bi = 0;
                                    // char
                                    int c;

                                    // this is boundary prefixed with \r\n--
                                    String __boundary = "\r\n--" + boundary;

                                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                    while ((c = clientInput.read()) != -1) {
                                        if ((char) c == __boundary.charAt(bi)) {
                                            buffer.write((char) c);
                                            bi++;
                                            if (bi == __boundary.length()) {
                                                out.close();
                                                break;
                                            }
                                        } else {
                                            if (buffer.size() != 0) {
                                                out.write(buffer.toString().getBytes());
                                                buffer.reset();
                                            } else {
                                                bi = 0;
                                            }
                                            out.write((char) c);
                                        }
                                    }

                                    if (c == -1) {
                                        file_upload.delete();
                                        Log.d("MYLOG", "Upload Failed");
                                        out.close();
                                        progressManager.reportStopped();
                                        return false;
                                    }
                                    out.close();
                                    progressManager.reportCompleted();
                                    return true;
                                } catch (Exception e) {
                                    progressManager.reportStopped();
                                }
                            }
                        }
                    }


                }

            }

        } catch (IOException ignore) {
        }
        return false;
    }

    private Map<String, String> parseAndSplit(String text) {
        Map<String, String> result = new TreeMap<>();
        String[] text_split = text.split("; ");
        for (String i : text_split) {
            if (i.contains("=")) {
                String[] key_value = i.split("=");
                if (key_value.length > 2) throw new RuntimeException("Wrong format");
                if (key_value[1].startsWith("\"") && key_value[1].endsWith("\"")) {
                    key_value[1] = key_value[1].substring(1, key_value[1].length() - 1);
                }
                result.put(key_value[0], key_value[1]);
            } else {
                result.put(i, "");
            }

        }
        return result;
    }

    private String method;
    private String path;
    private String version;
    private final Map<String, String> headers;
    private Map<String, String> params;

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

    /**
     * @noinspection unused
     */
    public Map<String, String> getParams() {
        return params;
    }

    public BufferedInputStream getClientInput() {
        return clientInput;
    }
    public String getHeader(String header) {
        return headers.get(header);
    }
    private boolean _connClose = false;
    public boolean isKeepAlive() {
        return !_connClose;
    }

}
