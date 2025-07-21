package com.ammar.sharing.network.websocket;

import android.app.KeyguardManager;
import android.util.Log;

import androidx.core.util.Function;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.exceptions.WebSocketException;
import com.ammar.sharing.network.websocket.sessions.WebSocketSession;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class WebSocket extends WebSocketImpl {

    private final WebSocketSession session;
    public WebSocket(Socket clientSocket, WebSocketSession session) {
        super(clientSocket);
        this.session = session;
    }

    public void sendText(String data) {
        new Thread(() -> {
            try {
                byte[] wsFrame = constructWebSocketFrame(data.getBytes(StandardCharsets.UTF_8), (byte) 0x1);
                clientSocket.getOutputStream().write(wsFrame);
            } catch (IOException ignore) {}
        }).start();
    }

    public void sendBinary(byte[] data) {
        new Thread(() -> {
            try {
                byte[] wsFrame = constructWebSocketFrame(data, (byte) 0x2);
                clientSocket.getOutputStream().write(wsFrame);
            } catch (IOException ignore) {}
        }).start();
    }

    @Override
    protected void onStringReceived(String data) {
        session.onMessage(this, data);
    }

    @Override
    protected void onBinaryReceived(byte[] data) {
        session.onMessage(this, data);
    }
}
