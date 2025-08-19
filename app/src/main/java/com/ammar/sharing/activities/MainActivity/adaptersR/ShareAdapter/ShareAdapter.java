package com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders.HeaderViewHolder;
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders.ProgressViewHolder;
import com.ammar.sharing.activities.MainActivity.fragments.BrowserShareFragment;
import com.ammar.sharing.custom.io.ProgressManager;

// this adapter for the recycler view you see when you open the app
// it takes the entire screen except for the top and bottom bars
public class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_PROGRESS = 2;
    private static final int TYPE_FOOTER = 3;

    private final BrowserShareFragment fragment;

    public ShareAdapter(BrowserShareFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        else if (position == getItemCount() - 1) return TYPE_FOOTER;
        else return TYPE_PROGRESS;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PROGRESS:
                LayoutInflater progressInflater = LayoutInflater.from(parent.getContext());
                View layout = progressInflater.inflate(R.layout.row_transfer_view, parent, false);
                return new ProgressViewHolder(layout);
            case TYPE_HEADER:
                LayoutInflater headerInflater = LayoutInflater.from(parent.getContext());
                View header = headerInflater.inflate(R.layout.row_transfer_header, parent, false);
                return new HeaderViewHolder(header, fragment);
            case TYPE_FOOTER: // this is bottom app bar
                Space space = new Space(parent.getContext());
                TypedValue out = new TypedValue();
                parent.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, out, true);
                int size = TypedValue.complexToDimensionPixelSize(out.data, parent.getContext().getResources().getDisplayMetrics());
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, size);
                space.setLayoutParams(params);
                return new RecyclerView.ViewHolder(space) {
                };
        }
        throw new RuntimeException("Invalid View Type in TransferAdapter");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_PROGRESS:
                ((ProgressViewHolder) holder).setup(ProgressManager.progresses.get(position - 1));
                break;
            case TYPE_HEADER:
                ((HeaderViewHolder) holder).updateUnseenMessagesNum();
                ((HeaderViewHolder) holder).updateViewCertButtonStatus();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return ProgressManager.progresses.size() + 2;
    }
}