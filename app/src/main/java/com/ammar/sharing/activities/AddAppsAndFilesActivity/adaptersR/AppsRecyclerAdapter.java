package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.AppViewHolder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.SearchBarViewHolder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.SpaceViewHolder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.AppData;
import com.ammar.sharing.common.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class AppsRecyclerAdapter extends SearchableRecyclerAdapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SEARCH_BAR = 0;
    private static final int TYPE_APP = 1;
    private static final int TYPE_SPACE = 2;

    private final AppData[] mAppsData;

    // if null all apps are listed
    @Nullable
    private ArrayList<Integer> mListedAppsIndices;
    public AppsRecyclerAdapter(AppData[] appsData) {
        this.mAppsData = appsData;
    }

    @Override
    public int getItemViewType(int position) {
        if( position == 0 ) {
            return TYPE_SEARCH_BAR;
        } else if (position == getItemCount() - 1){
            return TYPE_SPACE;
        } else {
            return TYPE_APP;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case TYPE_SEARCH_BAR:
                view = inflater.inflate(R.layout.view_search, parent, false);
                return new SearchBarViewHolder(view, this);
            case TYPE_APP:
                view = inflater.inflate(R.layout.view_add_apps_app, parent, false);
                return new AppViewHolder(view);
            case TYPE_SPACE:
                return SpaceViewHolder.MakeSpaceViewHolder(parent.getContext(), (int) Utils.dpToPx(140));
        }
        // It's impossible to reach this
        throw new RuntimeException("");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int positionR) {
        int type = getItemViewType(positionR);
        if (type == TYPE_APP) {

            int position = positionR - 1;
            if( mListedAppsIndices != null ) {
                position = mListedAppsIndices.get(position);
            }

            AppViewHolder appHolder = (AppViewHolder) holder;
            int size = (int) Utils.dpToPx(50);
            Glide.with(holder.itemView.getContext())
                    .load(mAppsData[position].icon)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(size, size)
                    .into(appHolder.getImageView());

            appHolder.getAppNameTV().setText(mAppsData[position].label);
            appHolder.getCheckBox().setChecked(mAppsData[position].isChecked);

            final int posCopy = position;
            appHolder.itemView.setOnClickListener((view) -> {
                boolean isChecked = appHolder.getCheckBox().isChecked();
                appHolder.getCheckBox().setChecked(!isChecked);
                this.mAppsData[posCopy].isChecked = !isChecked;
            });
        }
    }

    @Override
    public int getItemCount() {
        if( mListedAppsIndices != null ) return mListedAppsIndices.size() + 2;
        else return mAppsData.length + 2;
    }

    @Override
    public void searchItems(String text) {
        if (text.isEmpty()) {
            mListedAppsIndices = null;
            notifyDataSetChanged();
            return;
        }
        mListedAppsIndices = new ArrayList<>();
        for (int i = 0; i < mAppsData.length ; i++) {
            if (mAppsData[i].label.toLowerCase().contains(text.toLowerCase())) {
                mListedAppsIndices.add(i);
            }
        }
        notifyDataSetChanged();
    }
}
