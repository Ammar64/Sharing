package com.ammar.sharing.network.exceptions;

public class WebSocketException extends Exception {
    public WebSocketException(String message) {
        super(message);
    }

    public WebSocketException(Exception e) {
        super(e);
    }
}
