package com.ammar.filescenter.services.network.sessions;

import com.ammar.filescenter.services.network.Request;
import com.ammar.filescenter.services.network.Response;

public abstract class HTTPSession {
    private String[] paths;
    public HTTPSession(String[] paths) {
        this.paths = paths;
    }
    public abstract void GET(Request req, Response res);
    public abstract void POST(Request req, Response res);
    
    public String[] getPaths() {
        return this.paths;
    }

}
