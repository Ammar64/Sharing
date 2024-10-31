package com.ammar.sharing.network;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.SecurityUtils;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.exceptions.NotImplementedException;
import com.ammar.sharing.network.sessions.base.HTTPSession;
import com.ammar.sharing.network.utils.NetUtils;
import com.ammar.sharing.network.websocket.WebSocket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketException;
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

    public ClientHandler(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    // handle client here
    @Override
    public void run() {

        Request request = new Request(clientSocket);
        try {
            boolean upgradeToWebSocket = false;
            while (request.readSocket()) {
                // Check if it wants to upgrade to websocket
                if (isWebSocketUpgradeRequest(request)) {
                    upgradeToWebSocket = true;
                    Log.d("Websocket", "Upgrade to websocket");
                    break;
                } else {
                    handleNormalRequest(request);
                }
            }
            if (upgradeToWebSocket) {
                Log.d("MYLOG", "Upgraded to websocket");
                handleWebsocketUpgrade(request);
            }
        } catch (NotImplementedException e) {
            Response response = new Response(clientSocket);
            response.setStatusCode(501);
            response.setContentType("text/plain");
            response.sendResponse(("" + e.getMessage()).getBytes());
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

    private void handleNormalRequest(Request request) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Response response = new Response(clientSocket);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        response.setHeader("Connection", "keep-alive");
        // multiply timeout by 0.001 to convert from milliseconds into seconds
        response.setHeader("Keep-Alive", request.isKeepAlive() ? String.format(Locale.ENGLISH, "timeout=%d", (int) (timeout * 0.001)) : "close");

        String userAgent = request.getHeader("User-Agent");
        User user = User.RegisterUser(Utils.getSettings(), clientSocket, userAgent);
        if (!user.isBlocked()) {
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

    private void handleWebsocketUpgrade(Request request) {
        try {
            clientSocket.setSoTimeout(0);
            String userAgent = request.getHeader("User-Agent");
            User user = User.RegisterUser(Utils.getSettings(), clientSocket, userAgent);
            // prove that we received websocket handshake
            String wsKey = request.getHeader("Sec-WebSocket-Key").trim();
            String wsKeyPlusGUID = wsKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            String wsAccept = SecurityUtils.textToSha1Base64(wsKeyPlusGUID);
            Response response = new Response(request.getClientSocket());
            response.setHeader("Upgrade", "websocket");
            response.setHeader("Connection", "Upgrade");
            response.setHeader("Sec-WebSocket-Accept", wsAccept);
            response.setStatusCode(101);
            response.sendResponse();

            WebSocket ws = new WebSocket(clientSocket);
            user.setWebSocket(ws);
            ws.run();
        } catch (SocketException e) {
            Utils.showErrorDialog("ClientHandler.handleWebsocketUpgrade(): SocketException", e.getMessage());
        }
    }

    private boolean isWebSocketUpgradeRequest(Request req) throws NotImplementedException {
        String connection = req.getHeader("connection");
        String[] connectionValues = connection.split(", ");
        boolean isConnectionUpgrade = false;
        for (String i : connectionValues) {
            if ("Upgrade".equalsIgnoreCase(i)) isConnectionUpgrade = true;
        }

        String upgrade = req.getHeader("upgrade");
        boolean isWebSocketUpgrade = "websocket".equals(upgrade);
        if( !isWebSocketUpgrade && isConnectionUpgrade ) throw new NotImplementedException("Only supports websocket upgrade");
        return isWebSocketUpgrade && isConnectionUpgrade;
    }

    private void startSession(HTTPSession session, Request req, Response res) {
        if ("GET".equals(req.getMethod())) {
            session.GET(req, res);
        } else if ("POST".equals(req.getMethod())) {
            session.POST(req, res);
        }
    }

    private Class<? extends HTTPSession> inferSessionFromPath(String path) {
        for (Map.Entry<String, Class<? extends HTTPSession>> i : server.pathsMap.entrySet()) {
            String pathPattern = i.getKey();
            if (Pattern.matches(pathPattern, path)) {
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
                    Bitmap favBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.mipmap.ic_launcher_round, null));
                    res.sendBitmapResponse(favBM);
                } else if ("/common/almarai_regular.ttf".equals(req.getPath())) {
                    try {
                        res.setContentType("font/ttf");
                        res.sendResponse(Utils.readRawRes(R.raw.almarai_regular));
                    } catch (IOException e) {
                        Utils.showErrorDialog("PagesSession.GET(). IOException.", "Note: error happened when reading raw resources\n" + e.getMessage());
                    }
                } else if (!"/pages/blocked".equals(req.getPath())) {
                    res.setStatusCode(302);
                    res.setHeader("Content-Length", "0");
                    res.setHeader("Location", "/pages/blocked");
                    res.sendResponse();
                } else {
                    res.setStatusCode(403);
                    res.setHeader("Content-Type", "text/html");
                    String assetsPath = NetUtils.getCorrespondingAssetsPath("/pages/blocked", res);
                    res.sendResponse(Utils.readFileFromWebAssets(assetsPath));
                    res.close();
                }
            } catch (IOException e) {
                Utils.showErrorDialog("BlockedSession.GET(). IOException", "Failed to read from assets");
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
            res.setStatusCode(404);
            res.setHeader("Content-Type", "text/html");
            String assetsPath = NetUtils.getCorrespondingAssetsPath("/pages/404", res);
            try {
                res.sendResponse(Utils.readFileFromWebAssets(assetsPath));
            } catch (IOException e) {
                Utils.showErrorDialog("NotFoundSession.GET(). IOException", "Failed to read from assets");
            }
        }
    }

}
