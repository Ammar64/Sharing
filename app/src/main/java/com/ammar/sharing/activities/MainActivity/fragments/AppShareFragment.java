package com.ammar.sharing.activities.MainActivity.fragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AppToAppSharingActivity.AppToAppSharingActivity;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.common.utils.Utils;

public class AppShareFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main_appshare, container, false);
    }

    private ImageView mAppIconIV;
    private Button mSendButton;
    private Button mReceiveButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAppIconIV = view.findViewById(R.id.IV_SharingAppIcon);
        mSendButton = view.findViewById(R.id.B_Send);
        mReceiveButton = view.findViewById(R.id.B_Receive);

        ObjectAnimator appIconObjAnimator = ObjectAnimator.ofFloat(mAppIconIV, "rotation", 0f, 360f);
        appIconObjAnimator.setInterpolator(new LinearInterpolator());
        appIconObjAnimator.setRepeatMode(ObjectAnimator.RESTART);
        appIconObjAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        appIconObjAnimator.setDuration(60 * 1000);
        appIconObjAnimator.start();

        mSendButton.setOnClickListener((v) -> {
            Intent intent = new Intent(AppToAppSharingActivity.ACTION_SEND_TO_DEVICES);
            intent.setClass(this.requireContext(), AppToAppSharingActivity.class);
            startActivity(intent);
        });

        mReceiveButton.setOnClickListener((v) -> {
            Intent intent = new Intent(AppToAppSharingActivity.ACTION_RECEIVE_FROM_DEVICES);
            intent.setClass(this.requireContext(), AppToAppSharingActivity.class);
            startActivity(intent);
        });
    }
}
