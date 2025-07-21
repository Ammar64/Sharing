package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.FilesBrowserOptionData;
import com.ammar.sharing.activities.AddFilesActivity.AddFilesActivity;
import com.ammar.sharing.common.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class FilesBrowseOptionViewHolder extends RecyclerView.ViewHolder {

    private final ImageView mOptionIcon;
    private final TextView  mOptionTitle;
    private final TextView  mOptionDesc;
    private final ActivityResultLauncher<String> mGetMultipleContents;
    private final ActivityResultLauncher<Intent> mGetFilesBuiltIn;
    public FilesBrowseOptionViewHolder(@NonNull View itemView, ActivityResultLauncher<String> getMultipleContents, ActivityResultLauncher<Intent> getFilesBuiltIn) {
        super(itemView);
        mOptionIcon = itemView.findViewById(R.id.CIV_FilesBrowseOptionIcon);
        mOptionTitle = itemView.findViewById(R.id.TV_FilesBrowseOptionTitle);
        mOptionDesc = itemView.findViewById(R.id.TV_FilesBrowseOptionDesc);
        mGetMultipleContents = getMultipleContents;
        mGetFilesBuiltIn = getFilesBuiltIn;
    }

    public void setViewHolderData(FilesBrowserOptionData data) {
        int size = (int) Utils.dpToPx(45);
        Glide.with(itemView.getContext())
                .load(data.mIcon)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(size, size)
                .into(mOptionIcon);
        mOptionTitle.setText(data.mTitle);
        mOptionDesc.setText(data.mDesc);
        if( data.mPath == null ) {
            itemView.setOnClickListener((v) -> {
                mGetMultipleContents.launch("*/*");
            });
        } else {
            itemView.setOnClickListener((v) -> {
                Intent intent = new Intent(itemView.getContext(), AddFilesActivity.class);
                intent.putExtra(AddFilesActivity.EXTRA_ROOT_DIR, data.mPath);
                mGetFilesBuiltIn.launch(intent);
            });
        }
    }


}
