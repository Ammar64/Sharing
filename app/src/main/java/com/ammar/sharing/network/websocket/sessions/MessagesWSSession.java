package com.ammar.sharing.network.websocket.sessions;

import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders.HeaderViewHolder;
import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.MessagesAdapter;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.models.Message;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.WebSocket;

public class MessagesWSSession extends WebSocketSession {
    public MessagesWSSession(User user) {
        super(user);
    }

    @Override
    public void onMessage(WebSocket socket, String data) {
        super.onMessage(socket, data);
        Message message = Message.fromJSON(data, user);
        if( message != null ) {
            if( !user.getName().equals( message.getAuthorName() ) ) {
                message.setAuthorName(user.getName() + "!");
            }
            synchronized (MessagesAdapter.messages) {
                MessagesAdapter.messages.add(message);
                // notify UI that a message was received
                HeaderViewHolder.unseenMessagesCount++;
                Data.messagesNotifier.forcePostValue(MessagesAdapter.messages.size());
            }
            for( User i : User.users ) {
                if( i != this.user && i.isWebSocketConnected(MessagesWSSession.path) ) {
                    i.getWebSocket(MessagesWSSession.path).sendText(message.toJSON().toString());
                }
            }
        }
    }

    public static final String path = "/messages/ws";
}
