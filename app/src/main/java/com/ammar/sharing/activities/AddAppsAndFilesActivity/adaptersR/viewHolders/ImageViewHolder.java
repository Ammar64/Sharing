package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.MediaData;
import com.ammar.sharing.common.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    private final ImageView fileImageIV;
    private final View lineV;
    private final TextView fileNameTV;
    private final TextView fileSizeTV;
    private final TextView fileTypeNameTV;
    private final CheckBox fileCB;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        fileImageIV = itemView.findViewById(R.id.IV_FileImage);
        lineV = itemView.findViewById(R.id.V_Line);
        fileNameTV = itemView.findViewById(R.id.TV_FileName);
        fileSizeTV = itemView.findViewById(R.id.TV_FileSize);
        fileTypeNameTV = itemView.findViewById(R.id.TV_FileTypeName);
        fileCB = itemView.findViewById(R.id.CB_SelectFile);

    }

    public void setup(MediaData data) {
        lineV.setVisibility(View.INVISIBLE);
        fileTypeNameTV.setVisibility(View.GONE);
        fileImageIV.setPadding(0, 0, 0, 0);
        fileImageIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int size = (int) Utils.dpToPx(150);
        Glide.with(itemView.getContext())
                        .load(data.data)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(false)
                        .override(size)
                        .into(fileImageIV);

        fileNameTV.setText(data.name);
        fileSizeTV.setText(Utils.getFormattedSize(data.size));
        fileCB.setChecked(data.isChecked);

        itemView.setOnClickListener((view) -> {
            boolean isChecked = data.isChecked;
            data.isChecked = !isChecked;
            fileCB.setChecked(!isChecked);
        });
    }
}