package com.ammar.filescenter.activities.MainActivity.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity;
import com.ammar.filescenter.activities.AddFilesActivity;
import com.ammar.filescenter.activities.MainActivity.adapters.ReceiveAdapter;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.components.Server;

import java.util.ArrayList;


public class SendFragment extends Fragment {
    private View v;
    private ImageButton addAppsB;
    private ImageButton addFilesB;
    private ImageButton showSelected;
    private RecyclerView selectedDownloadsRV;
    private ReceiveAdapter adapter;


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getAction() != null) {
            Intent data = result.getData();
            if (data.getAction().equals(AddAppsActivity.ACTION_ADD_APPS)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(AddFilesActivity.EXTRA_INTENT_PATHS);
                // intent to be sent to service
                Intent intent = new Intent(getActivity(), NetworkService.class);
                intent.setAction(NetworkService.ACTION_ADD_DOWNLOADABLE);
                intent.putExtra(NetworkService.EXTRA_FILE_PATHS, selectedFilePaths);

            } else if (data.getAction().equals(AddFilesActivity.ACTION_ADD_FILES)) {
                Intent resultIntent = result.getData();
                ArrayList<String> selectedApps = resultIntent.getStringArrayListExtra(AddAppsActivity.EXTRA_INTENT_APPS);

                Intent intent = new Intent(getActivity(), NetworkService.class);
                intent.setAction(NetworkService.ACTION_ADD_APPS_DOWNLOADABLE);
                intent.putExtra(NetworkService.EXTRA_APPS_NAMES, selectedApps);
                if (getActivity() != null) getActivity().startService(intent);

            }
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_send, container, false);
        initItems();
        setItemsListener();
        return v;
    }

    private void initItems() {
        addAppsB = v.findViewById(R.id.B_AddApps);
        addFilesB = v.findViewById(R.id.B_AddFiles);
        showSelected = v.findViewById(R.id.B_ShowSelected);

        selectedDownloadsRV = v.findViewById(R.id.RV_SelectedDownloads);
        selectedDownloadsRV.setAdapter(adapter);
        selectedDownloadsRV.setLayoutManager(new LinearLayoutManager(getContext()));


    }

    private void setItemsListener() {

        addAppsB.setOnClickListener((button) -> {
            launcher.launch(new Intent(getActivity(), AddAppsActivity.class));
        });

        addFilesB.setOnClickListener((button) -> {
            launcher.launch(new Intent(getActivity(), AddFilesActivity.class));
        });

    }



}