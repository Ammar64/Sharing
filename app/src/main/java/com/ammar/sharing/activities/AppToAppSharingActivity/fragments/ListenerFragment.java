package com.ammar.sharing.activities.AppToAppSharingActivity.fragments;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ammar.sharing.R;
import com.ammar.sharing.tools.DevicesListener;

public class ListenerFragment extends Fragment {
    private DevicesListener mDevicesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDevicesListener = new DevicesListener();
        return inflater.inflate(R.layout.fragment_listener, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mDevicesListener.setOnDeviceListenedCallback((device) ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Device listened")
                        .setMessage("Device name: " + device.getName() + "\nDevice IP: " + device.getIpAddress() + "\nDevice OS: " + device.getOS().name())
                        .show()
        );
        mDevicesListener.setOnStartCommunicationCallback((ipSource, port, files) -> {
            Bundle args = new Bundle();
            args.putString(CommunicationFragment.REMOTE_SERVER_IP, ipSource);
            args.putInt(CommunicationFragment.REMOTE_SERVER_PORT, port);
            args.putStringArrayList(CommunicationFragment.FILES_LIST, files);
            Navigation.findNavController(view).navigate(R.id.action_listenerFragment_to_communicationFragment, args);
            return null;
        });
        mDevicesListener.listenToDevices();
    }

    @Override
    public void onDestroyView() {
        mDevicesListener.stop();
        super.onDestroyView();
    }
}