package com.ammar.sharing.activities.AddAppsAndFilesActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.fragments.AddAppsFragment;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.fragments.AddImagesFragment;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.fragments.AddVideosFragment;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.fragments.FilesFragment;
import com.ammar.sharing.common.utils.Utils;

public class AddAppsAndFilesViewPagerAdapter extends FragmentStateAdapter {
    private final Fragment[] fragments;
    private final String[] tabsText;

    public AddAppsAndFilesViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        fragments = new Fragment[]{new FilesFragment(), new AddAppsFragment(), new AddImagesFragment(), new AddVideosFragment()};
        tabsText = new String[] {
                Utils.getRes().getString(R.string.files),
                Utils.getRes().getString(R.string.apps),
                Utils.getRes().getString(R.string.images),
                Utils.getRes().getString(R.string.videos)
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public String getTabName(int pos) {
        return tabsText[pos];
    }
}
