package com.ammar.sharing.network.websocket;

import android.app.KeyguardManager;
import android.util.Log;

import androidx.core.util.Function;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.exceptions.WebSocketException;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class WebSocket extends WebSocketImpl {

    public WebSocket(Socket clientSocket) {
        super(clientSocket);
    }

    public void sendText(String data) {
        new Thread(() -> {
            try {
                byte[] wsFrame = constructWebSocketFrame(data.getBytes(StandardCharsets.UTF_8), (byte) 0x1);
                clientSocket.getOutputStream().write(wsFrame);
            } catch (IOException ignore) {}
        }).start();
    }

    public void sendBinary(byte[] data) throws IOException {
        new Thread(() -> {
            try {
                byte[] wsFrame = constructWebSocketFrame(data, (byte) 0x2);
                clientSocket.getOutputStream().write(wsFrame);
            } catch (IOException ignore) {}
        }).start();
    }

    public int addOnReceiveText(OnReceiveTextListener onReceive) {
        synchronized (onReceiveTextCallables) {
            int index = super.onReceiveTextCallables.size();
            super.onReceiveTextCallables.add(onReceive);
            return index;
        }
    }

    public int addOnReceiveBin(OnReceiveBinListener onReceive) {
        synchronized (onReceiveBinCallables) {
            int index = super.onReceiveBinCallables.size();
            super.onReceiveBinCallables.add(onReceive);
            return index;
        }
    }
}
