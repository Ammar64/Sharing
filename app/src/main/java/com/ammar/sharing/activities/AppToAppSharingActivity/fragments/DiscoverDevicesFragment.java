package com.ammar.sharing.activities.AppToAppSharingActivity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AppToAppSharingActivity.adaptersR.DiscoverDevicesAdapter;
import com.ammar.sharing.models.Device;
import com.ammar.sharing.tools.DevicesDiscoverer;

import java.util.ArrayList;

public class DiscoverDevicesFragment extends Fragment {
    private DevicesDiscoverer mDevicesDiscoverer;
    private final ArrayList<Device> mDevices = new ArrayList<>(3);
    private final DiscoverDevicesAdapter mAdapter = new DiscoverDevicesAdapter(mDevices);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDevicesDiscoverer = new DevicesDiscoverer();
        return inflater.inflate(R.layout.fragment_discover_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.RV_DiscoverDevices);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        mDevicesDiscoverer.setDeviceFoundCallback(this::addDevice);
        mDevicesDiscoverer.setGetTimeLeftCallback(499 , mAdapter::updateTimeLeftForNewBroadcastInfo);
        mDevicesDiscoverer.findDevices();
    }

    @Override
    public void onDestroyView() {
        mDevicesDiscoverer.stop();
        super.onDestroyView();
    }

    private void addDevice(Device device) {
        mDevices.add(device);
        mAdapter.notifyDataSetChanged();
    }

}
