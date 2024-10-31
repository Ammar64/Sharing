package com.ammar.sharing.network.websocket;

public class WebSocketFrame {
    boolean isFinal;
    /**
     * %x0 denotes a continuation frame
     * %x1 denotes a text frame
     * %x2 denotes a binary frame
     * %x3-7 are reserved for further non-control frames
     * %x8 denotes a connection close
     * %x9 denotes a ping
     * %xA denotes a pong
     * %xB-F are reserved for further control frames
     */
    byte opCode;
    boolean isMasked;
    long payloadLength;
    byte[] maskingKey;
    byte[] payloadData;


    public boolean isTextFrame() {
        return opCode == 0x1;
    }

    public boolean isBinaryFrame() {
        return opCode == 0x2;
    }

    public boolean isCloseFrame() {
        return opCode == 0x8;
    }

    public boolean isPing() {
        return opCode == 0x9;
    }
}