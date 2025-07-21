package com.ammar.sharing.activities.MessagesActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders.HeaderViewHolder;
import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.MessagesAdapter;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.custom.ui.AdaptiveActivity;
import com.ammar.sharing.models.Message;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.sessions.MessagesWSSession;

public class MessagesActivity extends AdaptiveActivity {

    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;
    private Button sendButton;
    private EditText messageInput;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        initItems();
        initObservers();
    }

    private void initItems() {
        HeaderViewHolder.unseenMessagesCount = 0;
        Toolbar toolbar = findViewById(R.id.TB_Toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(R.string.messages);
        toolbar.setNavigationOnClickListener((v) -> finish());

        messagesRecyclerView = findViewById(R.id.RV_MessagesRecyclerView);
        messagesAdapter = new MessagesAdapter();
        messagesRecyclerView.setAdapter(messagesAdapter);

        messageInput = findViewById(R.id.ET_MessageInput);

        sendButton = findViewById(R.id.B_MessageSend);
        sendButton.setOnClickListener((v) -> {
            String messageText = messageInput.getText().toString();
            if( messageText.isEmpty() ) return;
            Message message = new Message(messageText);
            for( User i : User.users ){
                if( i.isWebSokcetConnected(MessagesWSSession.path) ) {
                    i.getWebSocket(MessagesWSSession.path).sendText(message.toJSON().toString());
                }
            }
            synchronized (MessagesAdapter.messages) {
                MessagesAdapter.messages.add(message);
                messagesAdapter.notifyItemInserted(MessagesAdapter.messages.size() - 1);
                //FIXME: Scroll to bottom if user is already viewing the last message
                scrollToBottom();
            }
            messageInput.setText("");
        });
    }

    private void initObservers() {
        Data.messagesNotifier.observe(this, (size) -> {
            messagesAdapter.notifyItemInserted(size-1);
            //FIXME: Scroll to bottom if user is already viewing the last message
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        Log.d("MYLOG", "Scroll messages to bottom");
        messagesRecyclerView.smoothScrollToPosition(messagesAdapter.getItemCount()-1);
    }

    @Override
    protected void onPause() {
        HeaderViewHolder.unseenMessagesCount = 0;
        super.onPause();
    }
}
