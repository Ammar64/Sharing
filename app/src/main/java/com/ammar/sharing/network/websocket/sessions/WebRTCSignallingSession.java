package com.ammar.sharing.network.websocket.sessions;

import com.ammar.sharing.common.Data;
import com.ammar.sharing.custom.data.ObjectsHolder2;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

public class WebRTCSignallingSession extends WebSocketSession{
    public WebRTCSignallingSession(User user) {
        super(user);
    }
    @Override
    public void onMessage(WebSocket socket, String text) {
        super.onMessage(socket, text);
        try {
            JSONObject jsonObject = new JSONObject(text);
            String action = jsonObject.getString("action");
            if("ready-to-receive".equals(action)) {
                boolean isFirstTime = jsonObject.optBoolean("firstTime", false);
                Data.startStreamToUser.forcePostValue(new ObjectsHolder2<>(user, isFirstTime));
            }
        } catch (JSONException ignore) {}
    }


    public static final String path = "/web-rtc";
}
