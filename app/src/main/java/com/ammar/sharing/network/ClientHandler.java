package com.ammar.sharing.network;

import com.ammar.sharing.common.Utils;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.sessions.base.HTTPSession;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;


public class ClientHandler implements Runnable {
    /**
     * @noinspection FieldCanBeLocal
     */
    public static final int timeout = 5000;

    final Server server;
    private final Socket clientSocket;
    private User user = null;

    public ClientHandler(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    // handle client here
    @Override
    public void run() {

        Request request = new Request(clientSocket);
        try {
            while (request.readSocket()) {

                Response response = new Response(clientSocket);
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");

                // multiply timeout by 0.001 to convert from milliseconds into seconds
                response.setHeader("Keep-Alive", request.isKeepAlive() ? String.format(Locale.ENGLISH, "timeout=%d", (int) (timeout * 0.001)) : "close");

                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null)
                    user = User.RegisterUser(Utils.getSettings(), clientSocket, userAgent);
                if (user != null && !user.isBlocked()) {
                    Class<? extends HTTPSession> sessionClass = inferSessionFromPath(request.getPath());
                    if (sessionClass == null) {
                        startSession(new NotFoundSession(user), request, response);
                    } else {
                        HTTPSession session = sessionClass.getDeclaredConstructor(User.class).newInstance(user);
                        startSession(session, request, response);
                    }
                } else { // if user is blocked redirect to blocked page
                    startSession(new BlockedSession(user), request, response);
                }
            }
        } catch (Exception e) {
            Utils.showErrorDialog("ClientHandler.run(). Exception: ", e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Utils.showErrorDialog("ClientHandler.run().finally IOException: ", e.getMessage());
            }
        }
    }

    private void startSession(HTTPSession session, Request req, Response res) {
        if("GET".equals(req.getMethod())) {
            session.GET(req, res);
        } else if("POST".equals(req.getMethod())) {
            session.POST(req, res);
        }
    }

    private Class<? extends HTTPSession> inferSessionFromPath(String path) {
        for(Map.Entry<String, Class<? extends HTTPSession>> i : server.pathsMap.entrySet() ) {
            String pathPattern = i.getKey();
            if( Pattern.matches(pathPattern, path) ) {
                return i.getValue();
            }
        }
        return null;
    }


    // special sessions
    private static class BlockedSession extends HTTPSession {

        public BlockedSession(User user) {
            super(user);
        }

        @Override
        public void GET(Request req, Response res) {
            try {
                if ("/favicon.ico".equals(req.getPath())) {
                    res.setHeader("Content-Type", "image/svg+xml");
                    res.sendResponse(Utils.readFileFromWebAssets("icons8-share.svg"));
                } else if (!"/blocked".equals(req.getPath())) {
                    res.setStatusCode(307);
                    res.setHeader("Location", "/blocked");
                    res.sendResponse();
                    req.getClientSocket().close();
                } else {
                    res.setStatusCode(401);
                    res.setHeader("Content-Type", "text/html");
                    res.sendResponse(Utils.readFileFromWebAssets("blocked.html"));
                    res.close();
                }
            } catch (IOException e) {
                Utils.showErrorDialog("ClientHandler.GET(). IOException", "Failed to read from assets");
            }
        }
    }

    private static class NotFoundSession extends HTTPSession {
        public NotFoundSession(User user) {
            super(user);
        }

        @Override
        public void GET(Request req, Response res) {
            sendRes(res);
        }

        @Override
        public void POST(Request req, Response res) {
            sendRes(res);
        }

        private void sendRes(Response res) {
            try {
                res.setStatusCode(404);
                res.setHeader("Content-Type", "text/html");
                res.sendResponse("404 What are you doing ?".getBytes());
                res.close();
            } catch (IOException e) {
                Utils.showErrorDialog("NotFoundSession.sendRes(). IOException.", e.getMessage());
            }
        }
    }

}
