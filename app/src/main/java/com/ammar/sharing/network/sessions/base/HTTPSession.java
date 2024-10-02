package com.ammar.sharing.network.sessions.base;

import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;

import java.util.ArrayList;
import java.util.Arrays;

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
