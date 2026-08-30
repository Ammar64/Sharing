package com.ammar.sharing.models;

import com.ammar.sharing.common.enums.OS;
import com.ammar.sharing.custom.lambda.MyConsumer;
import com.ammar.sharing.network.websocket.WebSocket;
import com.ammar.sharing.network.websocket.sessions.WebSocketSession;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

@SuppressWarnings("JavaJniMissingFunction")
public class User {
    private String name;
    private String ip;
    private final int nativeIndex;
    private boolean isBlocked;
    private boolean isConnected;
    public User(String name, String ip, int nativeIndex, boolean isBlocked, boolean isConnected) {
        this.name = name;
        this.ip = ip;
        this.nativeIndex = nativeIndex;
        this.isBlocked = isBlocked;
        this.isConnected = isConnected;
    }
    private native void nativeSetBlocked(int index, boolean blocked);
    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
        nativeSetBlocked(nativeIndex, blocked);
    }

    private native int nativeGetOs(int index);
    public OS getOS() {
        int os_number = nativeGetOs(nativeIndex);
        return OS.fromInt(os_number);
    }

    public static native User getUser(int index);
    public static native int usersCount();
    public static native boolean noUsers();


    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public boolean isConnected() {
        return isConnected;
    }

}
