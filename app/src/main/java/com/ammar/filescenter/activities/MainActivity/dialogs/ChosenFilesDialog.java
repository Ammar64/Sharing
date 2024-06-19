package com.ammar.filescenter.activities.MainActivity.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.adapters.ChosenFilesAdapter;
import com.ammar.filescenter.services.NetworkService;

public class ChosenFilesDialog extends DialogFragment {
    public static final String TAG = "ChosenFilesDialog";

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_chosen_files, container, false);
        RecyclerView chosenFilesRV = v.findViewById(R.id.RV_ChosenFilesRecycler);
        ChosenFilesAdapter filesAdapter = new ChosenFilesAdapter();
        chosenFilesRV.setAdapter(filesAdapter);
        chosenFilesRV.setLayoutManager(new LinearLayoutManager(requireContext()));

        return v;
    }
}
