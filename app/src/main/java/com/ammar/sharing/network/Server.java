package com.ammar.sharing.network;

import android.util.Log;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.sessions.HTTPSession;
import com.ammar.sharing.network.websocket.sessions.WebSocketSession;
import com.ammar.sharing.services.ServerService;

import org.intellij.lang.annotations.RegExp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;

public class Server {

    public static int PORT_NUMBER;

    private ServerSocket serverSocket;
    private Thread serverThread;

    private boolean running = false;
    final ServerService service;

    final HashMap<String, Class<? extends HTTPSession>> pathsMap = new HashMap<>();
    final HashMap<String, Class<? extends WebSocketSession>> wsPathsMap = new HashMap<>();

    public Server(ServerService service) {
        this.service = service;
    }

    public void Start() {

        try {
            serverSocket = new ServerSocket(Server.PORT_NUMBER);
            serverThread = new Thread(this::Accept);
            serverThread.start();
            running = true;
        } catch (IOException e) {
            Utils.showErrorDialog("Server.Start(). Exception: ", e.getMessage());
        }
    }

    public void Stop() {
        try {
            serverSocket.close();
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
        for( @RegExp String i : paths ) {
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



    public boolean isRunning() {
        return running;
    }
}