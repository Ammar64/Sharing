package com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.viewHolders.MessageViewHolder;
import com.ammar.sharing.models.Message;

import java.util.ArrayList;


public class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    public static final ArrayList<Message> messages = new ArrayList<>(10);

    public MessagesAdapter() {
    }

    public static final int VIEW_TYPE_NOT_REMOTE = 0;
    public static final int VIEW_TYPE_REMOTE = 1;

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isRemote() ? VIEW_TYPE_REMOTE : VIEW_TYPE_NOT_REMOTE;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MessageViewHolder.Companion.constructNewMessageViewHolder(parent.getContext(), viewType == VIEW_TYPE_REMOTE);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.getContentTV().setText(message.getContent());
        if( holder.getAuthorTV() != null ) {
            holder.getAuthorTV().setText(message.getAuthor());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
