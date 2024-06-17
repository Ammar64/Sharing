package com.ammar.filescenter.services.components;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.models.Upload;
import com.ammar.filescenter.services.objects.Downloadable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
public class Server {

    public static final int PORT_NUMBER = 2999;

    private ServerSocket serverSocket;
    private Thread serverThread;

    public final LinkedList<Downloadable> downloadablesList = new LinkedList<>();
    public static final LinkedList<Upload> filesList = new LinkedList<>();
    public static final LinkedList<String> connectedDevices = new LinkedList<String>() {
        @Override
        public boolean add(String e) {
            if (!this.contains(e))
                return super.add(e);
            else return false;
        }
    };

    private boolean running = false;
    private Context context;

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
                ClientHandler clientHandler = new ClientHandler(context, downloadablesList, clientSocket);
                String clientAddress = clientSocket.getRemoteSocketAddress().toString();

                if (connectedDevices.add(clientAddress.split(":")[0])) {
                    // inform activity a new device connected
                    Intent intent = new Intent(NetworkService.ACTION_DEVICE_CONNECTED);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }

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