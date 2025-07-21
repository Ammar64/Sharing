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
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.MediaRecyclerAdapterPlaceholder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.MediaRecyclerAdapter;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.viewModels.ImagesViewModel;
import com.ammar.sharing.common.utils.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;

public class AddImagesFragment extends Fragment {

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
        mSpanCount = (int) Math.ceil(width / Utils.dpToPx(200));

        mShimmerContainer = root.findViewById(R.id.SFL_RecyclerContainer);

        mRecyclerView = root.findViewById(R.id.RV_AddAppsAndFiles);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), mSpanCount);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(new MediaRecyclerAdapterPlaceholder());

        mShimmerContainer.startShimmer();
    }

    private void setupViewModels() {
        ImagesViewModel imagesViewModel = new ViewModelProvider(this).get(ImagesViewModel.class);
        imagesViewModel.loadData();
        imagesViewModel.getImagesDataLiveData().observe(getViewLifecycleOwner(), (v) -> {
            MediaRecyclerAdapter adapter = new MediaRecyclerAdapter(v);
            ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return position == 0 || position == adapter.getItemCount() - 1 ? mSpanCount : 1;
                }
            });
            mRecyclerView.setAdapter(adapter);
        });

        imagesViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), (v) -> {
            if (!v) {
                mShimmerContainer.hideShimmer();
            }
        });
    }
}

