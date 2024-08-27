package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class PathViewHolder extends RecyclerView.ViewHolder{

    public static PathViewHolder makePathViewHolder(Context context) {
        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams( new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return new PathViewHolder(scrollView);
    }

    private PathViewHolder(@NonNull HorizontalScrollView itemView) {
        super(itemView);
    }
}
