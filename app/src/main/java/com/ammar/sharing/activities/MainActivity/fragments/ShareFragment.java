package com.ammar.sharing.activities.MainActivity.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsActivity.AddAppsActivity;
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.services.ServerService;

import java.util.ArrayList;


public class ShareFragment extends Fragment {
    private View v;
    private RecyclerView filesSendRV;
    private ShareAdapter adapter;

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_share, container, false);
        initItems();
        setItemsListener();
        initObservers();
        return v;
    }


    private void initItems() {
        filesSendRV = v.findViewById(R.id.RV_FilesSend);
        adapter = new ShareAdapter(this);
        filesSendRV.setAdapter(adapter);
        filesSendRV.setLayoutManager(new LinearLayoutManager(getContext()));
        filesSendRV.setItemAnimator(null);
        filesSendRV.setHasFixedSize(true);
    }

    private void setItemsListener() {
    }

    private void initObservers() {

        Data.filesSendNotifier.observe( requireActivity(), info -> {
            char action = info.getChar("action");
            int index = info.getInt("index");
            index += 1; // I don't remember why I increment 1 but it works like that :)
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

    public ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getAction() != null) {
            Intent data = result.getData();
            Intent intent = new Intent(requireContext(), ServerService.class);

            if (data.getAction().equals(Consts.ACTION_ADD_FILES)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(Consts.EXTRA_INTENT_PATHS);
                // intent to be sent to service
                intent.setAction(Consts.ACTION_ADD_FILE_SHARABLES);
                intent.putExtra(Consts.EXTRA_FILES_PATH, selectedFilePaths);
            } else if (data.getAction().equals(AddAppsActivity.ACTION_ADD_APPS)) {
                ArrayList<String> selectedApps = data.getStringArrayListExtra(AddAppsActivity.EXTRA_INTENT_APPS);

                intent.setAction(Consts.ACTION_ADD_APPS_SHARABLES);
                intent.putExtra(Consts.EXTRA_APPS_NAMES, selectedApps);
            }
            requireContext().startService(intent);

        }
    });


    public ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        Intent intent = result.getData();
        if ( result.getResultCode() == Activity.RESULT_OK && intent != null && Consts.ACTION_ADD_FILES.equals( intent.getAction())) {
           ArrayList<String> filesPath = intent.getStringArrayListExtra(Consts.EXTRA_FILES_PATH);
           Intent serviceIntent = new Intent(requireContext(), ServerService.class);
           serviceIntent.setAction(Consts.ACTION_ADD_FILE_SHARABLES);
           serviceIntent.putStringArrayListExtra(Consts.EXTRA_FILES_PATH, filesPath);
           requireContext().startService(serviceIntent);
        }
    });
}