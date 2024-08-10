package com.ammar.sharing.activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ammar.sharing.activities.MainActivity.fragments.SettingsFragment;
import com.ammar.sharing.activities.MainActivity.fragments.ShareFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {
    private final Fragment[] fragments;

    public MainViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        fragments = new Fragment[]{new ShareFragment(), new SettingsFragment()};
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
}
