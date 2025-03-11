package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.EmptyViewHolder;

public class AppsRecyclerAdapterPlaceholder extends RecyclerView.Adapter<EmptyViewHolder> {
    @NonNull
    @Override
    public EmptyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_add_apps_loading, parent, false);
        return new EmptyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmptyViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 40;
    }

}
