package com.ammar.filescenter.activities.MainActivity.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity.AddAppsActivity;
import com.ammar.filescenter.activities.MainActivity.MainActivity;
import com.ammar.filescenter.activities.MainActivity.adaptersR.TransferAdapter;
import com.ammar.filescenter.common.Vals;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.common.Utils;

import java.util.ArrayList;


public class TransferFragment extends Fragment {
    private View v;
    private Toolbar toolbar;
    private RecyclerView filesSendRV;
    private TransferAdapter adapter;

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
        v = inflater.inflate(R.layout.fragment_transfer, container, false);
        initItems();
        setItemsListener();
        initObservers();
        return v;
    }


    private void initItems() {
        toolbar = v.findViewById(R.id.TB_Toolbar);
        toolbar.setTitle(R.string.share);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        filesSendRV = v.findViewById(R.id.RV_FilesSend);
        adapter = new TransferAdapter(this);
        filesSendRV.setAdapter(adapter);
        filesSendRV.setLayoutManager(new LinearLayoutManager(getContext()));
        filesSendRV.setItemAnimator(null);
        filesSendRV.setHasFixedSize(true);
    }

    private void setItemsListener() {
    }

    private void initObservers() {

        NetworkService.filesSendNotifier.observe( requireActivity(), info -> {
            char action = info.getChar("action");
            int index = info.getInt("index");
            index += 1;
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return MainActivity.onOptionsItemSelectedStatic(requireActivity(), item);
    }

    public ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getAction() != null) {
            Intent data = result.getData();
            Intent intent = new Intent(requireContext(), NetworkService.class);

            if (data.getAction().equals(Vals.ACTION_ADD_FILES)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(Vals.EXTRA_INTENT_PATHS);
                // intent to be sent to service
                intent.setAction(Vals.ACTION_ADD_DOWNLOADS);
                intent.putExtra(Vals.EXTRA_FILE_PATHS, selectedFilePaths);
            } else if (data.getAction().equals(AddAppsActivity.ACTION_ADD_APPS)) {
                ArrayList<String> selectedApps = data.getStringArrayListExtra(AddAppsActivity.EXTRA_INTENT_APPS);

                intent.setAction(Vals.ACTION_ADD_APPS_DOWNLOADS);
                intent.putExtra(Vals.EXTRA_APPS_NAMES, selectedApps);
            }
            requireContext().startService(intent);

        }
    });


    public ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), (result) -> {
        ArrayList<String> files = new ArrayList<>(result.size());

        for( Uri i : result ) {
            String path = Utils.getPathFromUri(requireContext(), i);
            files.add(path);
        }

        Intent intent = new Intent(requireContext(), NetworkService.class);

        // intent to be sent to service
        intent.setAction(Vals.ACTION_ADD_DOWNLOADS);
        intent.putExtra(Vals.EXTRA_FILE_PATHS, files);
        requireContext().startService(intent);
    });

}