package com.ammar.sharing.network;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.sessions.AppConfigSession;
import com.ammar.sharing.network.sessions.CLISession;
import com.ammar.sharing.network.sessions.DownloadSession;
import com.ammar.sharing.network.sessions.SharedAssetsSession;
import com.ammar.sharing.network.sessions.HTTPSession;
import com.ammar.sharing.network.sessions.MessagesSession;
import com.ammar.sharing.network.sessions.NoJSSession;
import com.ammar.sharing.network.sessions.RedirectSession;
import com.ammar.sharing.network.sessions.UploadSession;
import com.ammar.sharing.network.sessions.UserSession;
import com.ammar.sharing.network.websocket.sessions.InfoWSSession;
import com.ammar.sharing.network.websocket.sessions.MainWSSession;
import com.ammar.sharing.network.websocket.sessions.MessagesWSSession;
import com.ammar.sharing.network.websocket.sessions.WebSocketSession;
import com.ammar.sharing.services.ServerService;

import org.intellij.lang.annotations.RegExp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;

import javax.net.ssl.SSLServerSocket;

public class Server {

    public static int PORT_NUMBER;
    public static boolean IS_HTTPS;
    private ServerSocket serverSocket;
    private SSLServerSocket sslServerSocket;
    private SSLServerSocketManager sslServerSocketManager;
    private Thread serverThread;

    private boolean running = false;
    final ServerService service;

    final HashMap<String, Class<? extends HTTPSession>> pathsMap = new HashMap<>();
    final HashMap<String, Class<? extends WebSocketSession>> wsPathsMap = new HashMap<>();

    public Server(ServerService service) {
        this.service = service;
        handleSessionsData();
        sslServerSocketManager = SSLServerSocketManager.getInstance();
    }

    public void Start() {
        try {
            if( Server.IS_HTTPS ) {
                sslServerSocket = sslServerSocketManager.generateSSLServerSocket();
                serverThread = new Thread(this::AcceptSSL);
                serverThread.start();
            } else {
                serverSocket = new ServerSocket(Server.PORT_NUMBER);
                serverThread = new Thread(this::Accept);
                serverThread.start();
            }
            running = true;
        } catch (IOException e) {
            Utils.showErrorDialog("Server.Start(). Exception: ", e.getMessage());
        }
    }

    public void Stop() {
        try {
            if( sslServerSocket != null ) {
                sslServerSocket.close();
            }
            if( serverSocket != null ) {
                serverSocket.close();
            }
            running = false;
        } catch (IOException e) {
            Utils.showErrorDialog("Server.Stop(). Exception: ", e.getMessage());
        } finally {
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                Utils.showErrorDialog("Server.Stop(). Exception", "Server.Stop(). InterruptedException:  " + e.getMessage());
            }
        }
    }

    /// @param pathPattern regex is supported
    public void addPath(@RegExp String pathPattern, Class<? extends HTTPSession> sessionClass) {
        pathsMap.put(pathPattern, sessionClass);
    }

    /// @param paths regex is supported
    public void addPaths(Collection<String> paths, Class<? extends HTTPSession> sessionClass) {
        for (@RegExp String i : paths) {
            addPath(i, sessionClass);
        }
    }

    /// @param path regex is NOT supported
    public void addWebsocketPath(String path, Class<? extends WebSocketSession> wsSessionClass) {
        wsPathsMap.put(path, wsSessionClass);
    }

    private void Accept() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clientSocket.setKeepAlive(true);
                clientSocket.setSoTimeout(ClientHandler.timeout);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (SocketException e) {
            // TODO: Socket probably closed   
        } catch (IOException e) {
            Utils.showErrorDialog("IOException", "Server.Accept(). IOException: " + e.getMessage());
        }
    }

    private void AcceptSSL() {
        try {
            while (!sslServerSocket.isClosed()) {
                Socket clientSocket = sslServerSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clientSocket.setKeepAlive(true);
                clientSocket.setSoTimeout(ClientHandler.timeout);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (SocketException e) {
            // TODO: Socket probably closed
        } catch (IOException e) {
            Utils.showErrorDialog("IOException", "Server.AcceptSSL(). IOException: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }

    void handleSessionsData() {
        // NoJS
        this.addPath("/no-JS", NoJSSession.class);
        // DownloadSession
        this.addPath("/download/(.*)", DownloadSession.class);
        this.addPath("/available-downloads", DownloadSession.class);

        // UploadSession
        this.addPath("/upload/(.*)", UploadSession.class);

        // UserSession
        this.addPath("/get-user-info", UserSession.class);
        this.addPath("/update-user-name", UserSession.class);

        // CLI Session
        this.addPath("/ls", CLISession.class);
        this.addPath("/dl/(.*)", CLISession.class);
        this.addPath("/da", CLISession.class);

        //DynamicAssetsSession
        this.addPath("/get-icon/(.*)", SharedAssetsSession.class);
        this.addPath("/favicon.ico", SharedAssetsSession.class);

        this.addPath("/get-all-messages", MessagesSession.class);

        //AppConfigSession
        this.addPath("/check-upload-allowed", AppConfigSession.class);
        this.addPath("/get-app-config", AppConfigSession.class);

        this.addPaths(RedirectSession.redirectMap.keySet(), RedirectSession.class);
        this.addWebsocketPath(MainWSSession.path, MainWSSession.class);
        this.addWebsocketPath(MessagesWSSession.path, MessagesWSSession.class);
        this.addWebsocketPath(InfoWSSession.path, InfoWSSession.class);
    }
}