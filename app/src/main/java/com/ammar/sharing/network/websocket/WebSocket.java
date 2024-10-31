package com.ammar.sharing.network.websocket;

import android.app.KeyguardManager;
import android.util.Log;

import androidx.core.util.Function;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.exceptions.WebSocketException;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

    public void setOnReceiveText(OnReceiveTextListener onReceive) {
        super.onReceiveTextCallable = onReceive;
    }

    public void setOnReceiveBin(OnReceiveBinListener onReceive) {
        super.onReceiveBinCallable = onReceive;
    }
}
