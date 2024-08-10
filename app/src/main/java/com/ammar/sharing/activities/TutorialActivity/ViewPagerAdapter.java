package com.ammar.sharing.activities.TutorialActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ammar.sharing.activities.TutorialActivity.fragments.TutorialFragment;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private ArrayList<FragmentHolder> holders;
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, ArrayList<FragmentHolder> holders) {
        super(fragmentManager, lifecycle);
        this.holders = holders;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new TutorialFragment(holders.get(position));
    }

    @Override
    public int getItemCount() {
        return holders.size();
    }
}
