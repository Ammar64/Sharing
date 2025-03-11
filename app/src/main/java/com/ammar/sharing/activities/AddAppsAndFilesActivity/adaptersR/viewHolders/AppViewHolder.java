package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;

public class AppViewHolder extends RecyclerView.ViewHolder {
    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.IV_AppIcon);
        mCheckBox = itemView.findViewById(R.id.CB_AppCheckBox);
        mAppName = itemView.findViewById(R.id.TV_AppName);
    }

    ImageView mIcon;
    CheckBox mCheckBox;
    TextView mAppName;


    public TextView getAppNameTV() {
        return mAppName;
    }

    public CheckBox getCheckBox() {
        return mCheckBox;
    }

    public ImageView getImageView() {
        return mIcon;
    }
}
