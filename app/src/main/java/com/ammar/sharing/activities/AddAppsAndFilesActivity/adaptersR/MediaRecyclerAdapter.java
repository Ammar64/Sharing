package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.ImageViewHolder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.SearchBarViewHolder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.SpaceViewHolder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.MediaData;
import com.ammar.sharing.common.utils.Utils;

import java.util.ArrayList;

public class MediaRecyclerAdapter extends SearchableRecyclerAdapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SEARCH_BAR = 0;
    private static final int TYPE_MEDIA = 1;
    private static final int TYPE_SPACE = 2;

    private final MediaData[] mMediaData;

    // if null all images are listed
    @Nullable
    private ArrayList<Integer> mListedMediaIndices;
    public MediaRecyclerAdapter(MediaData[] mediaData) {
        this.mMediaData = mediaData;
    }

    @Override
    public int getItemViewType(int position) {
        if( position == 0 ) {
            return TYPE_SEARCH_BAR;
        } else if (position == getItemCount() - 1){
            return TYPE_SPACE;
        } else {
            return TYPE_MEDIA;
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
            case TYPE_MEDIA:
                view = inflater.inflate(R.layout.view_file, parent, false);
                return new ImageViewHolder(view);
            case TYPE_SPACE:
                return SpaceViewHolder.MakeSpaceViewHolder(parent.getContext(), (int) Utils.dpToPx(140));
        }
        // It's impossible to reach this
        throw new RuntimeException("");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int positionR) {
        int type = getItemViewType(positionR);
        if (type == TYPE_MEDIA) {
            int position = positionR - 1;
            if( mListedMediaIndices != null ) {
                position = mListedMediaIndices.get(position);
            }
            ImageViewHolder imageHolder = (ImageViewHolder) holder;
            imageHolder.setup(mMediaData[position]);
        }
    }

    @Override
    public int getItemCount() {
        if( mListedMediaIndices != null ) return mListedMediaIndices.size() + 2;
        else return mMediaData.length + 2;
    }

    @Override
    public void searchItems(String text) {
        if (text.isEmpty()) {
            mListedMediaIndices = null;
            notifyDataSetChanged();
            return;
        }
        mListedMediaIndices = new ArrayList<>();
        for (int i = 0; i < mMediaData.length ; i++) {
            if (mMediaData[i].name.toLowerCase().contains(text.toLowerCase())) {
                mListedMediaIndices.add(i);
            }
        }
        notifyDataSetChanged();
    }
}
