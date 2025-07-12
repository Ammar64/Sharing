package com.ammar.sharing.activities.MainActivity.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;

public class AppShareFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main_appshare, container, false);
    }

    private ImageView mAppIconIV;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAppIconIV = view.findViewById(R.id.IV_SharingAppIcon);

        ObjectAnimator appIconObjAnimator = ObjectAnimator.ofFloat(mAppIconIV, "rotation", 0f, 360f);
        appIconObjAnimator.setInterpolator(new LinearInterpolator());
        appIconObjAnimator.setRepeatMode(ObjectAnimator.RESTART);
        appIconObjAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        appIconObjAnimator.setDuration(60 * 1000);
        appIconObjAnimator.start();
    }
}
