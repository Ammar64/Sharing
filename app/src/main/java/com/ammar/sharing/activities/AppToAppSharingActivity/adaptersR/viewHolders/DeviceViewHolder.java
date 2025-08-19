package com.ammar.sharing.activities.AppToAppSharingActivity.adaptersR.viewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.Device;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class DeviceViewHolder extends RecyclerView.ViewHolder {
    private final ImageView mDeviceIcon;
    private final TextView mDeviceName;
    public DeviceViewHolder(@NonNull View itemView) {
        super(itemView);
        mDeviceIcon = itemView.findViewById(R.id.IV_DeviceIcon);
        mDeviceName = itemView.findViewById(R.id.TV_DeviceName);
    }

    public void setup(Device device) {
        mDeviceName.setText(device.getName());

        @DrawableRes
        int deviceIconRes = switch (device.getOS()) {
            case ANDROID, IOS -> R.drawable.ic_mobile;
            case LINUX, WINDOWS, MAC -> R.drawable.ic_laptop;
            default -> R.drawable.icon_question_mark;
        };
        int iconSize = (int) Utils.dpToPx(30);
        Glide.with(itemView.getContext())
                .load(deviceIconRes)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(iconSize)
                .into(mDeviceIcon);
    }
}
