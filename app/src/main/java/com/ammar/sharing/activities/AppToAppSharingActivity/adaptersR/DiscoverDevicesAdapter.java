package com.ammar.sharing.activities.AppToAppSharingActivity.adaptersR;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AppToAppSharingActivity.adaptersR.viewHolders.DeviceViewHolder;
import com.ammar.sharing.models.Device;

import java.util.ArrayList;

public class DiscoverDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DEVICE = 0;
    private static final int VIEW_TYPE_INFO = 1;

    private final ArrayList<Device> mDevices;
    public DiscoverDevicesAdapter(ArrayList<Device> devices) {
        mDevices = devices;
    }

    @Override
    public int getItemViewType(int position) {
        // TEMP
        return  VIEW_TYPE_DEVICE;
        //return position < mDevices.size() ? VIEW_TYPE_DEVICE : VIEW_TYPE_INFO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_DEVICE:
                DeviceViewHolder deviceHolder = (DeviceViewHolder) holder;
                Device device = mDevices.get(position);
                deviceHolder.setup(device);
                break;
            case VIEW_TYPE_INFO:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
        //return mDevices.size() + 1;
    }

    public void updateTimeLeftForNewBroadcastInfo(long timeLeft) {
        // TODO: Implement
    }


}
