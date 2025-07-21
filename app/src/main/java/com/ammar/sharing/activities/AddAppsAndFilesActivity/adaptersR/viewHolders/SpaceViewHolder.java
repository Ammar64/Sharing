package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceViewHolder extends RecyclerView.ViewHolder {

    public static SpaceViewHolder MakeSpaceViewHolder(Context context , int height) {
        Space spaceView = new Space(context);
        spaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return new SpaceViewHolder(spaceView);
    }

    private SpaceViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
