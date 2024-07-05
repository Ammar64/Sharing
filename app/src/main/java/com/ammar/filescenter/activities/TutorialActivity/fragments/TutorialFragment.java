package com.ammar.filescenter.activities.TutorialActivity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.TutorialActivity.FragmentHolder;

public class TutorialFragment extends Fragment {
    FragmentHolder holder;

    public TutorialFragment(FragmentHolder holder) {
        this.holder = holder;
    }

    private View v;
    private ImageView bg;
    private ImageView icon;
    private TextView title;
    private TextView desc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tutorial_card, container, false);
        initItems();
        initStates();
        return v;
    }

    private void initItems() {
        bg = v.findViewById(R.id.IV_CardBG);
        icon = v.findViewById(R.id.IV_Icon);
        title = v.findViewById(R.id.TV_Title);
        desc = v.findViewById(R.id.TV_Desc);
    }

    private void initStates() {
        bg.setImageResource(holder.getBackgroundImageId());
        icon.setImageResource(holder.getIconId());
        title.setText(holder.getTitleId());
        desc.setText(holder.getDescId());
    }
}
