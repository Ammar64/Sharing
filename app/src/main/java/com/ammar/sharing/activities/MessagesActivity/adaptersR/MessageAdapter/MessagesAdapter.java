package com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.viewHolders.MessageViewHolder;
import com.ammar.sharing.custom.data.QueueMutableLiveData;
import com.ammar.sharing.models.Message;

import java.lang.ref.WeakReference;
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
        LinearLayout linearLayout = MessageViewHolder.Companion.constructNewMessageView(parent.getContext(), viewType == VIEW_TYPE_REMOTE);
        return new MessageViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.getTextView().setText(messages.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
