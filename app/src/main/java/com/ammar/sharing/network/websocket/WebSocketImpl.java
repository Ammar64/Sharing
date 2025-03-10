package com.ammar.sharing.network.websocket;

import android.os.Handler;
import android.util.Log;

import androidx.core.util.Function;

import com.ammar.sharing.common.utils.TypesUtils;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.network.exceptions.WebSocketException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

import kotlin.collections.ArraysKt;

public abstract class WebSocketImpl {
    protected final Socket clientSocket;
    protected boolean running = true;

    public static final byte CLOSE = 0x8;
    public static final byte PING = 0x9;
    public static final byte PONG = 0xa;
    private static final byte[] ALLOWED_OPCODES = new byte[]{
            0x0, // continuation frame
            0x1, // text frame
            0x2, // binary frame
            0x8, // connection close
            0x9, // ping
            0xa  // pong
    };

    private Thread closeThread;
    protected final ArrayList<OnReceiveTextListener> onReceiveTextCallables = new ArrayList<>();
    protected final ArrayList<OnReceiveBinListener> onReceiveBinCallables = new ArrayList<>();
    private boolean isCloseSent = false;
    public WebSocketImpl(Socket clientSocket) {
        this.clientSocket = clientSocket;
        closeThread = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return;
            }
            running = false;
            try {
                clientSocket.close();
            } catch (IOException ignore) {}

        });
    }

    // 64 MB
    public static final int MAX_PAYLOAD_SIZE = 64 * 1024 * 1024;

    protected WebSocketFrame readWebSocketFrame() throws WebSocketException {
        try {
            InputStream input = clientSocket.getInputStream();
            WebSocketFrame wsf = new WebSocketFrame();
            int b;
            // FIN
            b = input.read();
            wsf.isFinal = (b & 0x80) != 0;
            if (!wsf.isFinal) {
                throw new WebSocketException("non final websocket frames are not supported by the server");
            }
            // RSV 1,2,3
            // make sure reserved bytes are 0
            if ((b & 0x70) != 0) {
                throw new WebSocketException("Reserved bytes are not zeros");
            }

            // opCode
            wsf.opCode = (byte) (b & 0x0f);
            if (!ArraysKt.contains(ALLOWED_OPCODES, wsf.opCode)) {
                throw new WebSocketException("Invalid websocket opCode");
            }

            // is masked
            b = input.read();
            wsf.isMasked = (b & 0x80) != 0;
            if (!wsf.isMasked) {
                throw new WebSocketException("Client websocket frames must be masked");
            }

            // payload length
            int payloadLength7bits = (b & 0x7f);
            Log.d("Websocket", "payloadLength7bits: " + payloadLength7bits);
            if (payloadLength7bits <= 125) {
                wsf.payloadLength = payloadLength7bits;
            } else if (payloadLength7bits == 126) {
                byte[] payloadLengthBuffer = new byte[2];
                int bytesRead = input.read(payloadLengthBuffer);
                // safety
                if (bytesRead != 2) {
                    throw new WebSocketException("Invalid WebSocketFrame expected to read 2 bytes actual read " + bytesRead);
                }

                wsf.payloadLength = TypesUtils.byteArrayToShort(payloadLengthBuffer);
                Log.d("Websocket", "payloadLength16bits: " + wsf.payloadLength);

            } else if (wsf.payloadLength == 127) {
                byte[] payloadLengthBuffer = new byte[8];
                int bytesRead = input.read(payloadLengthBuffer);
                // safety
                if (bytesRead != 2) {
                    throw new WebSocketException("Invalid WebSocketFrame expected to read 8 bytes actual read " + bytesRead);
                }
                wsf.payloadLength = TypesUtils.byteArrayToLong(payloadLengthBuffer);

                if ((wsf.payloadLength & 0x8000000000000000L) != 0) {
                    throw new WebSocketException("Most significant bit must be 0");
                }
            }
            if (wsf.payloadLength > MAX_PAYLOAD_SIZE) {
                throw new WebSocketException("Payload length is too big. Payload length is: " + wsf.payloadLength + ". Max value: " + MAX_PAYLOAD_SIZE);
            }
            // MASKING KEY
            byte[] maskBuffer = new byte[4];
            int bytesRead = input.read(maskBuffer);
            if (bytesRead != 4) {
                throw new WebSocketException("Invalid WebSocketFrame expected to read 4 bytes actual read " + bytesRead);
            }
            wsf.maskingKey = maskBuffer;

            // PAYLOAD DATA
            byte[] payload = new byte[(int) wsf.payloadLength];
            input.read(payload);
            // unmask payload
            for (int i = 0; i < wsf.payloadLength; i++) {
                payload[i] = (byte) (payload[i] ^ wsf.maskingKey[i & 0x3]);
            }

            wsf.payloadData = payload;

            // return decoded frame
            return wsf;

        } catch (IOException e) {
            throw new WebSocketException("");
        }
    }

    protected native byte[] constructWebSocketFrame(byte[] data, byte opCode);

    public void run() {
        while (running) {
            try {
                WebSocketFrame wsf = readWebSocketFrame();
                if( wsf.isCloseFrame() ) {
                    if( !isCloseSent ) {
                        sendControlFrame(CLOSE, null);
                    } else {
                        closeThread.interrupt();
                    }
                    running = false;
                    Log.d("Websocket", "Socket closed");
                } else if( wsf.isPing() ) {
                    byte[] rawWSF = constructWebSocketFrame(wsf.payloadData, PONG);
                    clientSocket.getOutputStream().write(rawWSF);
                } else if( wsf.isTextFrame() ) {
                    for( OnReceiveTextListener i : onReceiveTextCallables ) {
                        i.apply(new String(wsf.payloadData, StandardCharsets.UTF_8));
                    }
                } else if (wsf.isBinaryFrame()) {
                    for( OnReceiveBinListener i : onReceiveBinCallables ) {
                        i.apply(wsf.payloadData);
                    }
                }
            } catch (WebSocketException e) {
                sendControlFrame(CLOSE, null);
                running = false;
                Utils.showErrorDialog("WebSocket error.", e.getMessage());
            } catch (Exception e) {
                sendControlFrame(CLOSE, null);
                running = false;
                Utils.showErrorDialog("ERROR", e.getMessage());
            }
        }
    }

    public void close() {
        sendControlFrame(CLOSE, null);
        isCloseSent = true;
        closeThread.start();
    }

    public boolean isNotClosed() {
        return !clientSocket.isClosed();
    }

    protected void sendControlFrame(byte opCode, byte[] payload) {
        OutputStream out = null;
        try {
            out = clientSocket.getOutputStream();
            byte[] closeFrameRaw = constructWebSocketFrame(payload, (byte)0x8);
            out.write(closeFrameRaw);
        } catch (IOException ignore) {
        }
    }


    @FunctionalInterface
    public interface OnReceiveTextListener {
        void apply(String data);
    }

    @FunctionalInterface
    public interface OnReceiveBinListener {
        void apply(byte[] data);
    }
}

