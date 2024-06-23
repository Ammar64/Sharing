package com.ammar.filescenter.services.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.ammar.filescenter.activities.MainActivity.fragments.SettingsFragment;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.models.User;
import com.ammar.filescenter.services.network.sessions.base.HTTPSession;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Locale;


public class ClientHandler implements Runnable {
    /**
     * @noinspection FieldCanBeLocal
     */
    public static final int timeout = 5000;
    private final Socket clientSocket;

    private final Context context;
    private final SharedPreferences settings;

    private User user = null;

    public ClientHandler(Context context, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.context = context;
        this.settings = this.context.getSharedPreferences(SettingsFragment.SettingsPrefFile, Context.MODE_PRIVATE);
    }

    private final BlockedSession blockedSession = new BlockedSession();

    // handle client here
    @Override
    public void run() {

        try {
            Request request = new Request(clientSocket);
            while (request.readSocket()) {

                Response response = new Response(clientSocket);
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                // multiply timeout by 0.001 to convert from milliseconds into seconds
                response.setHeader("Keep-Alive", request.isKeepAlive() ? String.format(Locale.ENGLISH, "timeout=%d", (int) (timeout * 0.001)) : "close");

                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null)
                    user = User.RegisterUser(settings, clientSocket.getRemoteSocketAddress(), userAgent);
                if (user != null && !user.isBlocked()) {
                    for (HTTPSession i : HTTPSession.sessions) {
                        if(handleSession(i, request, response)) break;
                    }
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

    private static class BlockedSession extends HTTPSession {

        public BlockedSession() {
            super();
        }

        @Override
        public void GET(Request req, Response res) {
            try {
                if (!"/blocked".equals(req.getPath())) {
                    res.setStatusCode(307);
                    res.setHeader("Location", "/blocked");
                    res.sendResponse();
                } else {
                    res.setStatusCode(401);
                    res.setHeader("Content-Type", "text/html");
                    res.sendResponse(Utils.readFileFromAssets("blocked.html"));
                    req.getClientSocket().close();
                }
            } catch (IOException e) {
                Utils.showErrorDialog("ClientHandler.GET(). IOException", "Failed to read from assets");
            }
        }
    }
}
