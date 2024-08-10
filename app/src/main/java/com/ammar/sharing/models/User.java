package com.ammar.sharing.models;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Data;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class User {
    public static final ArrayList<User> users = new ArrayList<>();

    private final int id;
    public final LinkedList<Socket> sockets = new LinkedList<>();
    private final SocketAddress address;
    private boolean _isBlocked = false;


    private String name;

    private static int numUsers = 0;

    private User(Socket socket, String userAgent) {
        this.address = socket.getRemoteSocketAddress();
        this.sockets.add(socket);

        this.id = numUsers++;
        this.name = "User-" + getId();

        if( userAgent.contains("Windows") ) {
            this.OS = Consts.OS.WINDOWS;
        } else if( userAgent.contains("Android") ) {
            this.OS = Consts.OS.ANDROID;
        } else if ( userAgent.contains("Linux") ) {
            this.OS = Consts.OS.LINUX;
        } else this.OS = Consts.OS.UNKNOWN;
    }
    // make new user if user exist return it.
    public static User RegisterUser(SharedPreferences prefs, Socket socket, String agent) {
        User registered_user = getUserBySockAddr(socket.getRemoteSocketAddress());
        if( registered_user != null ) {
            if( !registered_user.socketExists(socket) )
                registered_user.addSocket(socket);
            return registered_user;
        }
        else {
            User new_user = new User(socket, agent);
            User.users.add(new_user);
            // block or not
            new_user.block( prefs.getBoolean(Consts.PREF_FIELD_ARE_USER_BLOCKED, false) );

            // inform UI
            Bundle bundle = new Bundle();
            bundle.putChar("action", 'A');
            bundle.putInt("index", new_user.getId());
            Data.usersListObserver.forcePostValue(bundle);
            return new_user;
        }
    }

    private boolean socketExists(Socket s) {
        return sockets.contains(s);
    }

    @Nullable
    public static User getUserBySockAddr(SocketAddress targetAddr) {
        String targetIp = targetAddr.toString();
        targetIp = targetIp.substring(1, targetIp.lastIndexOf(":"));
        for( User i : users) {
            String ip = i.getAddress().toString();
            ip = ip.substring(1, ip.lastIndexOf(":"));
            if( ip.equals(targetIp) ) {
                return i;
            }
        }
        return null;
    }

    public static void closeAllSockets() {
        try {
            for (User user : users) {
                Iterator<Socket> iterator = user.sockets.iterator();
                while ( iterator.hasNext() ) {
                    Socket i = iterator.next();
                    i.getInputStream().close();
                    i.getOutputStream().close();
                    i.close();
                    iterator.remove();
                }
            }
        } catch (IOException ignore) {}

    }

    public void addSocket(Socket s) {
        sockets.add(s);
    }
    public void removeSocket(Socket clientSocket) {
        sockets.remove(clientSocket);
    }
    public void setName(String name) {
        this.name = name;

        Bundle bundle = new Bundle();
        bundle.putChar("action", 'C');
        bundle.putInt("index", getId());
        Data.usersListObserver.forcePostValue(bundle);
    }
    public void block(boolean b) {
        this._isBlocked = b;
    }

    public int getId() {
        return id;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public String getIp() {
        String ip = address.toString();
        return ip.substring(1, ip.lastIndexOf(":"));
    }
    private final Consts.OS OS;
    public Consts.OS getOS() {
        return OS;
    }
    public boolean isBlocked() {
        return this._isBlocked;
    }
    public String getName() {
        return name;
    }


}
