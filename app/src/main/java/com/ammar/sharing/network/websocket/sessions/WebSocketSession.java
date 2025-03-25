package com.ammar.sharing.network.websocket.sessions;

import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.WebSocket;

abstract public class WebSocketSession {
    protected final User user;
    public WebSocketSession(User user) {
        this.user = user;
    }

    public void onMessage(WebSocket socket, byte[] data) {}
    public void onMessage(WebSocket socket, String text) {}
}
