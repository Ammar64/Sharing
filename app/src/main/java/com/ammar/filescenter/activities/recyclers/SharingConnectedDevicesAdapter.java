package com.ammar.filescenter.activities.recyclers;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SharingConnectedDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> connectedDevices;
    private Context context;
    public SharingConnectedDevicesAdapter(Context context, List<String> devices) {
        this.context = context;
        connectedDevices = devices;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new IpViewHolder(new TextView(context));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int position) {
        IpViewHolder holder = (IpViewHolder) baseHolder;
        holder.deviceIP.setText(connectedDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return connectedDevices.size();
    }

    private static class IpViewHolder extends RecyclerView.ViewHolder {
        public TextView deviceIP;

        public IpViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIP = (TextView) itemView;
            deviceIP.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            deviceIP.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

        }
    }
}
