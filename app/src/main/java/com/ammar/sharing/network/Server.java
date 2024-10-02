package com.ammar.sharing.network;

import android.util.Log;

import com.ammar.sharing.network.sessions.base.HTTPSession;
import com.ammar.sharing.services.ServerService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class Server {

    public static final int PORT_NUMBER = 2999;

    private ServerSocket serverSocket;
    private Thread serverThread;

    private boolean running = false;
    final ServerService service;
    final HashMap<String, Class<? extends HTTPSession>> pathsMap = new HashMap<>();

    public Server(ServerService service) {
        this.service = service;
    }

    public void Start() {

        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
            serverThread = new Thread(this::Accept);
            serverThread.start();
            running = true;
        } catch (IOException e) {
            Log.e("MYLOG", "Server.Start(): " + e.getMessage());
        }
    }

    public void Stop() {
        try {
            serverSocket.close();
            running = false;
        } catch (IOException e) {
            Log.e("MYLOG","Server.Stop(). IOException: " + e.getMessage());
        } finally {

            try {
                serverThread.join();
            } catch (InterruptedException e) {
                Log.d("MYLOG", "Server.Stop(). InterruptedException:  " + e.getMessage());
            }
        }
    }

    public void addPath(String pathPattern, Class<? extends HTTPSession> sessionClass) {
        pathsMap.put(pathPattern, sessionClass);
    }

    private void Accept() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (SocketException e) {
            // TODO: Socket probably closed   
        } catch (IOException e) {
            Log.e("MYLOG", "Server.Accept(). IOException: " + e.getMessage());
        }
    }



    public boolean isRunning() {
        return running;
    }
}