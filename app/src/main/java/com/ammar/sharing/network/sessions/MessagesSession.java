package com.ammar.sharing.network.sessions;

import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.MessagesAdapter;
import com.ammar.sharing.models.Message;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;

import java.nio.charset.StandardCharsets;

public class MessagesSession extends HTTPSession {
    public MessagesSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        String path = req.getPath();
        if("/get-all-messages".equals(path)) {
            String jsonRes = Message.toJSONArray(MessagesAdapter.messages).toString();
            res.setContentType("application/json");
            res.sendResponse(jsonRes.getBytes(StandardCharsets.UTF_8));
        }
    }
}
