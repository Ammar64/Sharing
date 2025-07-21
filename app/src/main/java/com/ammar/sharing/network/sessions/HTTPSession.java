package com.ammar.sharing.network.sessions;

import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;

public abstract class HTTPSession {

    protected final User user;

    public HTTPSession(User user) {
        this.user = user;
    }

    public void GET(Request req, Response res) {
    }
    public void POST(Request req, Response res) {
    }

}
