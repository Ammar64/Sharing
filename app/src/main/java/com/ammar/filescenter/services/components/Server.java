package com.ammar.filescenter.services.components;

import android.util.Log;

import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.components.ClientHandler;
import com.ammar.filescenter.services.components.Request;
import com.ammar.filescenter.services.objects.Downloadable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

public class Server {
    public static final int PORT_NUMBER = 2999;

    private ServerSocket serverSocket;
    private Thread serverThread;
    public final LinkedList<Downloadable> downloadablesList = new LinkedList<>();

    private boolean running = false;

    public void Start() {
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
            serverThread = new Thread(this::Accept);
            serverThread.start();
            running = true;
        } catch (IOException e) {
            Log.e("MYLOG", e.getMessage());
        }
    }

    public void Stop() {
        try {
            serverSocket.close();
            running = false;
        } catch (IOException e) {
            Log.e("MYLOG", e.getMessage());
        } finally {

            try {
                serverThread.join();
            } catch (InterruptedException e) {
                Log.d("MYLOG", e.getMessage());
            }
        }
    }

    private void Accept() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(downloadablesList, clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (SocketException e) {
            // TODO: Socket probably closed   
        } catch (IOException e) {
            Log.e("MYLOG", e.getMessage());
        }
    }


    public boolean isRunning() {
        return running;
    }
}