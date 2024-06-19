package com.ammar.filescenter.services.models;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ammar.filescenter.services.NetworkService;

import java.net.SocketAddress;
import java.util.ArrayList;

public class User {
    public static final ArrayList<User> users = new ArrayList<>();
    private final int id;
    private final SocketAddress address;
    private String userAgent;



    private String name;

    private static int numUsers = 0;

    private User(SocketAddress address, String userAgent) {
        this.address = address;
        this.userAgent = userAgent;
        this.id = numUsers++;
        this.name = "User-" + id;
    }
    // make new user if user exist return it.
    public static User RegisterUser(SocketAddress address, String agent) {
        User registered_user = getUserBySockAddr(address);
        if( registered_user != null )
            return registered_user;
        else {
            User new_user = new User(address, agent);
            User.users.add(new_user);
            // inform UI
            Bundle bundle = new Bundle();
            bundle.putChar("action", 'A');
            bundle.putInt("index", new_user.getId());
            NetworkService.usersListObserver.postValue(bundle);
            return new_user;
        }
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

    public void setName(String name) {
        this.name = name;
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

    public Object getName() {
        return name;
    }
}
