package com.ammar.sharing.activities.AppToAppSharingActivity.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ammar.sharing.R;

import java.util.ArrayList;

class CommunicationFragment extends Fragment {
    public static String REMOTE_SERVER_IP = "REMOTE_SERVER_IP";
    public static String REMOTE_SERVER_PORT = "REMOTE_SERVER_PORT";
    public static String FILES_LIST = "FILES_LIST";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if( args == null ) {
            throw new RuntimeException("CommunicationFragment() called without args");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_communication, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        assert args != null;
        String ip = args.getString(REMOTE_SERVER_IP);
        int port = args.getInt(REMOTE_SERVER_PORT);
        ArrayList<String> files = args.getStringArrayList(FILES_LIST);


    }
}