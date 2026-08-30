package com.ammar.sharing.network.websocket.sessions;

import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.WebSocket;

public class InfoWSSession extends WebSocketSession {
    public InfoWSSession(User user) {
        super(user);
    }

    @Override
    public void onMessage(WebSocket socket, String data) {
        super.onMessage(socket, data);
    }

    public static final String path = "/info";
}
