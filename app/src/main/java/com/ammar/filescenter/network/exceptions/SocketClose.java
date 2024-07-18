package com.ammar.filescenter.network.exceptions;

public class SocketClose extends RuntimeException {
    public SocketClose() {
        super("Socket probably closed");
    }
}
