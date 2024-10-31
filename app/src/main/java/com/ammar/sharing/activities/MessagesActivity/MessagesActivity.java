package com.ammar.sharing.activities.MessagesActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.MessagesAdapter;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.custom.ui.AdaptiveActivity;
import com.ammar.sharing.models.Message;
import com.ammar.sharing.models.User;

import java.lang.ref.WeakReference;

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
        Toolbar toolbar = findViewById(R.id.TB_Toolbar);
        toolbar.setTitle(R.string.messages);

        messagesRecyclerView = findViewById(R.id.RV_MessagesRecyclerView);
        messagesAdapter = new MessagesAdapter();
        messagesRecyclerView.setAdapter(messagesAdapter);

        messageInput = findViewById(R.id.ET_MessageInput);

        sendButton = findViewById(R.id.B_MessageSend);
        sendButton.setOnClickListener((v) -> {
            String message = messageInput.getText().toString();
            for( User i : User.users ){
                if( i.isConnectedViaWebSocket() ) {
                    i.getWebSocket().sendText(message);
                }
            }
            synchronized (MessagesAdapter.messages) {
                MessagesAdapter.messages.add(new Message(message, false));
                boolean isLastVisible = isLastVisible();
                messagesAdapter.notifyItemInserted(MessagesAdapter.messages.size() - 1);
                if( isLastVisible ) {
                    scrollToBottom();
                }
                messageInput.setText("");
            }
        });
    }

    private void initObservers() {
        Data.messagesNotifier.observe(this, (position) -> {
            boolean isLastVisible = isLastVisible();
            messagesAdapter.notifyItemInserted(position);
            if( isLastVisible ) {
                scrollToBottom();
            }
        });
    }

    private void scrollToBottom() {
        Log.d("MYLOG", "Scroll messages to bottom");
        messagesRecyclerView.smoothScrollToPosition(messagesAdapter.getItemCount()-1);
    }

    boolean isLastVisible() {
        return true;
    }

}
