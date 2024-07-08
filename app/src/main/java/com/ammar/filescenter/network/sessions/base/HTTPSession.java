package com.ammar.filescenter.network.sessions.base;

import com.ammar.filescenter.models.User;
import com.ammar.filescenter.network.Request;
import com.ammar.filescenter.network.Response;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class HTTPSession {
    private final ArrayList<String> paths;
    private boolean isPathsParent;
    protected User user = null;

    public HTTPSession(String[] paths) {
        HTTPSession.sessions.add(this);
        this.isPathsParent = false;
        if (paths == null) {
            this.paths = null;
            return;
        }
        //Arrays.sort( paths );
        this.paths = new ArrayList<>(Arrays.asList(paths));
    }

    public static ArrayList<HTTPSession> sessions = new ArrayList<>();

    public HTTPSession(String[] paths, boolean isPathsParent) {
        this(paths);
        this.isPathsParent = isPathsParent;
    }

    public HTTPSession() {
        this.paths = null;
        isPathsParent = false;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void GET(Request req, Response res) {
    }

    public void POST(Request req, Response res) {
    }

    public ArrayList<String> getPaths() {
        return this.paths;
    }

    public boolean isParentPath() {
        return isPathsParent;
    }

}
