package com.ammar.sharing.network.websocket.sessions;

import com.ammar.sharing.models.User;

public class MainWSSession extends WebSocketSession{
    public MainWSSession(User user) {
        super(user);
    }

    public static final String path = "/ws";
}
