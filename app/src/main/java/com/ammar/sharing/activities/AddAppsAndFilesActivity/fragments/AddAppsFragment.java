package com.ammar.sharing.activities.AddAppsAndFilesActivity.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.AppsRecyclerAdapter;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.AppsRecyclerAdapterPlaceholder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.viewModels.AppsViewModel;
import com.ammar.sharing.common.utils.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;

public class AddAppsFragment extends Fragment {

    View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_add_apps_and_files_recycler_fragment, container, false);
        setupFragment();
        setupViewModels();
        return root;
    }

    ShimmerFrameLayout mShimmerContainer;
    RecyclerView mRecyclerView;

    private int mSpanCount;

    private void setupFragment() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        mSpanCount = (int) Math.ceil(width / Utils.dpToPx(130));

        mShimmerContainer = root.findViewById(R.id.SFL_RecyclerContainer);

        mRecyclerView = root.findViewById(R.id.RV_AddAppsAndFiles);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), mSpanCount);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(new AppsRecyclerAdapterPlaceholder());

        mShimmerContainer.startShimmer();
    }

    private void setupViewModels() {
        AppsViewModel appsViewModel = new ViewModelProvider(this).get(AppsViewModel.class);
        appsViewModel.loadData();
        appsViewModel.getAppsLiveData().observe(getViewLifecycleOwner(), (v) -> {
            AppsRecyclerAdapter adapter = new AppsRecyclerAdapter(v);
            ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return position == 0 || position == adapter.getItemCount() - 1 ? mSpanCount : 1;
                }
            });
            mRecyclerView.setAdapter(adapter);
        });

        appsViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), (v) -> {
            if(!v) {
                mShimmerContainer.hideShimmer();
            }
        });
    }
}
