package com.ammar.filescenter.network;

import android.content.Context;
import android.util.Log;

import com.ammar.filescenter.models.Transferable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
public class Server {

    public static final int PORT_NUMBER = 2999;

    private ServerSocket serverSocket;
    private Thread serverThread;

    public static final LinkedList<Transferable> filesList = new LinkedList<>();
    private boolean running = false;
    private final Context context;

    public Server(Context context) {
        this.context = context;
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

    private void Accept() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(context, clientSocket);

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