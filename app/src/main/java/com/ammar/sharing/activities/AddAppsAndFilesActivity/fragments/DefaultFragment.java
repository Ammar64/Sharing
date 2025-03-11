package com.ammar.sharing.activities.AddAppsAndFilesActivity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.AddAppsAndFilesActivity;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.DefaultFragmentRecyclerAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;

public class DefaultFragment extends Fragment {

    View root;

    RecyclerView mRecyclerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_add_apps_and_files_recycler_fragment, container, false);
        setupFragment();
        return root;
    }

    private void setupFragment() {
        ((ShimmerFrameLayout)root.findViewById(R.id.SFL_RecyclerContainer)).hideShimmer();
        mRecyclerView = root.findViewById(R.id.RV_AddAppsAndFiles);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(new DefaultFragmentRecyclerAdapter(this, ((AddAppsAndFilesActivity)requireActivity())));
    }

}