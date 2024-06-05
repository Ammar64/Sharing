package com.ammar.filescenter.activities.MainActivity.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity;
import com.ammar.filescenter.activities.AddFilesActivity;
import com.ammar.filescenter.activities.MainActivity.adapters.SendAdapter;
import com.ammar.filescenter.activities.MainActivity.dialogs.ChosenFilesDialog;
import com.ammar.filescenter.services.NetworkService;

import java.util.ArrayList;


public class SendFragment extends Fragment {
    private View v;
    private ImageButton addAppsB;
    private ImageButton addFilesB;
    private ImageButton showSelected;
    private TextView serverLinkTV;
    private RecyclerView filesSendRV;
    private SendAdapter adapter;

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
        initObservers();
        return v;
    }


    private void initItems() {
        addAppsB = v.findViewById(R.id.B_AddApps);
        addFilesB = v.findViewById(R.id.B_AddFiles);
        showSelected = v.findViewById(R.id.B_ShowSelected);
        serverLinkTV = v.findViewById(R.id.TV_ServerLink);

        filesSendRV = v.findViewById(R.id.RV_FilesSend);
        adapter = new SendAdapter();
        filesSendRV.setAdapter(adapter);
        filesSendRV.setLayoutManager(new LinearLayoutManager(getContext()));
        filesSendRV.setItemAnimator(null);
        filesSendRV.setHasFixedSize(true);


    }

    private void setItemsListener() {

        addAppsB.setOnClickListener((button) -> {
            launcher.launch(new Intent(getActivity(), AddAppsActivity.class));
        });

        addFilesB.setOnClickListener((button) -> {
            launcher.launch(new Intent(getActivity(), AddFilesActivity.class));
        });

        showSelected.setOnClickListener(button -> {
            ChosenFilesDialog dialog = new ChosenFilesDialog();
            dialog.show(requireActivity().getSupportFragmentManager(), ChosenFilesDialog.TAG);
        });

    }

    private void initObservers() {
        NetworkService.filesListObserver.observe(getViewLifecycleOwner(), data -> {
        });

        NetworkService.filesSendNotifier.observe( requireActivity(), info -> {
            char action = info.getChar("action");
            int index = info.getInt("index");

            switch (action) {
                case 'P':
                    adapter.notifyItemChanged(index);
                    break;
                case 'R':
                    adapter.notifyItemRemoved(index);
                    break;
                case 'A':
                    adapter.notifyItemInserted(index);
                    break;
            }


        });

    }


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getAction() != null) {
            Intent data = result.getData();
            Intent intent = new Intent(requireContext(), NetworkService.class);

            if (data.getAction().equals(AddFilesActivity.ACTION_ADD_FILES)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(AddFilesActivity.EXTRA_INTENT_PATHS);
                // intent to be sent to service
                intent.setAction(NetworkService.ACTION_ADD_DOWNLOADS);
                intent.putExtra(NetworkService.EXTRA_FILE_PATHS, selectedFilePaths);
            } else if (data.getAction().equals(AddAppsActivity.ACTION_ADD_APPS)) {
                ArrayList<String> selectedApps = data.getStringArrayListExtra(AddAppsActivity.EXTRA_INTENT_APPS);

                intent.setAction(NetworkService.ACTION_ADD_APPS_DOWNLOADS);
                intent.putExtra(NetworkService.EXTRA_APPS_NAMES, selectedApps);
            }
            requireContext().startService(intent);

        }
    });
}