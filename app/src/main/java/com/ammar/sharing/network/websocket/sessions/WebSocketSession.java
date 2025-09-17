package com.ammar.sharing.network.websocket.sessions;

import androidx.annotation.CallSuper;

import com.ammar.sharing.custom.lambda.MyConsumer;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.WebSocket;

abstract public class WebSocketSession {
    protected final User user;
    public WebSocketSession(User user) {
        this.user = user;
    }

    @CallSuper
    public void onMessage(WebSocket socket, byte[] data) {
        if(mOnBinaryMessageListener != null) {
            mOnBinaryMessageListener.accept(data);
        }
    }

    @CallSuper
    public void onMessage(WebSocket socket, String text) {
        if(mOnStringMessageListener != null) {
            mOnStringMessageListener.accept(text);
        }
    }


    private MyConsumer<String> mOnStringMessageListener;
    private MyConsumer<byte[]> mOnBinaryMessageListener;

    public void setOnStringMessageListener(MyConsumer<String> l) {
        mOnStringMessageListener = l;
    }

    public void setOnBinaryMessageListener(MyConsumer<byte[]> l) {
        mOnBinaryMessageListener = l;
    }
}
