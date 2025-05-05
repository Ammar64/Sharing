package com.ammar.sharing.activities.AddAppsAndFilesActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.AppData;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.MediaData;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.viewModels.AppsViewModel;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.viewModels.ImagesViewModel;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.viewModels.VideosViewModel;
import com.ammar.sharing.custom.ui.DefaultActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class AddAppsAndFilesActivity extends DefaultActivity {

    // bool extras
    public static final String EXTRA_APPS_SHARED = "EXTRA_APPS_SHARED";
    public static final String EXTRA_URIS_SHARED = "EXTRA_URIS_SHARED";
    public static final String EXTRA_FILE_PATHS_SHARED = "EXTRA_FILE_PATHS_SHARED";
    // string arraylist
    public static final String EXTRA_PACKAGES_NAMES = "EXTRA_PACKAGES_NAMES";
    public static final String EXTRA_FILES_PATHS = "EXTRA_FILES_PATHS";
    public static final String EXTRA_URIS = "EXTRA_URIS";
    //
    private Toolbar mAppBar;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager2;
    private FloatingActionButton mDoneFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_apps_and_files);
        setupActivity();
    }

    AppsViewModel appsViewModel;
    ImagesViewModel imagesViewModel;
    VideosViewModel videosViewModel;

    private void setupActivity() {
        appsViewModel = new ViewModelProvider(this).get(AppsViewModel.class);
        imagesViewModel = new ViewModelProvider(this).get(ImagesViewModel.class);
        videosViewModel = new ViewModelProvider(this).get(VideosViewModel.class);

        mAppBar = findViewById(R.id.TB_Toolbar);
        setSupportActionBar(mAppBar);
        mAppBar.setNavigationIcon(R.drawable.icon_back);

        mTabLayout = findViewById(R.id.TL_AppsAndFiles);

        mViewPager2 = findViewById(R.id.VP2_AddAppsAndFiles);
        AddAppsAndFilesViewPagerAdapter viewPagerAdapter = new AddAppsAndFilesViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        mViewPager2.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager2, (tab, pos) -> tab.setText(viewPagerAdapter.getTabName(pos))).attach();

        mDoneFAB = findViewById(R.id.FAB_PickingAppsAndFilesDone);

        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mDoneFAB.setVisibility(View.GONE);
                } else {
                    mDoneFAB.setVisibility(View.VISIBLE);
                }
            }
        });

        mDoneFAB.setOnClickListener((v) -> {
            Intent intent = new Intent();
            // apps
            ArrayList<String> selectedApps = new ArrayList<>();
            AppData[] appsData = appsViewModel.getAppsLiveData().getValue();
            if (appsData != null) {

                for (AppData i : appsData) {
                    if (i.isChecked) {
                        selectedApps.add(i.packageName);
                        intent.putExtra(EXTRA_APPS_SHARED, true);
                    }
                }
                intent.putExtra(EXTRA_PACKAGES_NAMES, selectedApps);
            }

            ArrayList<Uri> selectedMedia = new ArrayList<>();
            // images
            MediaData[] imagesData = imagesViewModel.getImagesDataLiveData().getValue();
            if (imagesData != null) {
                for (MediaData i : imagesData) {
                    if (i.isChecked) {
                        selectedMedia.add(i.data);
                    }
                }
            }
            // videos
            MediaData[] videosData = videosViewModel.getVideosDataLiveData().getValue();
            if (videosData != null) {
                for (MediaData i : videosData) {
                    if (i.isChecked) {
                        selectedMedia.add(i.data);
                    }
                }
            }

            appsViewModel.reset();
            imagesViewModel.reset();
            videosViewModel.reset();

            if (!selectedMedia.isEmpty()) {
                intent.putExtra(EXTRA_URIS_SHARED, true);
                intent.putExtra(EXTRA_URIS, selectedMedia);
            }
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appsViewModel.reset();
        imagesViewModel.reset();
        videosViewModel.reset();
    }

}

