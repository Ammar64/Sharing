package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR;

import androidx.recyclerview.widget.RecyclerView;

abstract public class SearchableRecyclerAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    public abstract void searchItems(String text);
}
