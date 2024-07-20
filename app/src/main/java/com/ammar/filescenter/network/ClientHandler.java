package com.ammar.filescenter.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.models.User;
import com.ammar.filescenter.network.sessions.base.HTTPSession;
import com.ammar.filescenter.services.ServerService;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;


public class ClientHandler implements Runnable {
    /**
     * @noinspection FieldCanBeLocal
     */
    public static final int timeout = 5000;
    private final Socket clientSocket;

    private final SharedPreferences settings;

    private User user = null;

    public ClientHandler(ServerService service, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.settings = service.getSharedPreferences(Consts.PREF_SETTINGS, Context.MODE_PRIVATE);
    }

    private final BlockedSession blockedSession = new BlockedSession();
    private final NotFoundSession notFoundSession = new NotFoundSession();

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
                    user = User.RegisterUser(settings, clientSocket, userAgent);
                if (user != null && !user.isBlocked()) {
                    boolean found = false;
                    for (HTTPSession i : HTTPSession.sessions) {
                        found = handleSession(i, request, response);
                        if (found) break;
                    }
                    if (!found) handleSession(notFoundSession, request, response);
                } else  // if user is blocked redirect to blocked page
                    handleSession(blockedSession, request, response);
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

    private boolean handleSession(HTTPSession session, Request req, Response res) {
        session.setUser(user);
        boolean isRequestedSession = false;
        if (session.getPaths() != null) {
            if (session.isParentPath()) {
                // not that fast
                for (String i : session.getPaths()) {
                    if (req.getPath().startsWith(i)) {
                        isRequestedSession = true;
                        break;
                    }
                }
            } else {
                // fast
                isRequestedSession = session.getPaths().contains(req.getPath());
            }
        } else isRequestedSession = true;
        if (isRequestedSession) {
            if ("GET".equals(req.getMethod()))
                session.GET(req, res);
            else if ("POST".equals(req.getMethod()))
                session.POST(req, res);
        }
        return isRequestedSession;
    }


    // special sessions
    private static class BlockedSession extends HTTPSession {

        public BlockedSession() {
            super();
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
        public NotFoundSession() {
            super();
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
