package com.ammar.sharing.network.websocket;

import com.ammar.sharing.network.websocket.sessions.WebSocketSession;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class WebSocket extends WebSocketImpl {

    private final WebSocketSession mSession;
    public WebSocket(Socket clientSocket, WebSocketSession session) {
        super(clientSocket);
        this.mSession = session;
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

    public WebSocketSession getSession() {
        return mSession;
    }

    @Override
    protected void onStringReceived(String data) {
        mSession.onMessage(this, data);
    }

    @Override
    protected void onBinaryReceived(byte[] data) {
        mSession.onMessage(this, data);
    }
}
