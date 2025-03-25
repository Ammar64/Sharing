package com.ammar.sharing.activities.StreamingActivity.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.ammar.sharing.R;
import com.ammar.sharing.custom.ui.PermissionRequester;
import com.ammar.sharing.services.ScreenCapturerService;
import com.google.android.material.button.MaterialButton;

public class NavFragment extends Fragment {
    PermissionRequester cameraPermissionRequester;
    PermissionRequester audioPermissionRequester;

    ActivityResultLauncher<Intent> screenCapturePermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() != Activity.RESULT_OK) {
            //TODO: User didn't give permission to capture the screen.
            return;
        }
        Intent mediaPermissionData = result.getData();
        Bundle args = new Bundle();
        args.putParcelable(ScreenFragment.ARG_MEDIA_PERMISSION_DATA, mediaPermissionData);
        if (Build.VERSION.SDK_INT >= 28) {
            Intent intent = new Intent(requireContext(), ScreenCapturerService.class);
            requireContext().startForegroundService(intent);
        }

        new Handler().postDelayed(() -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.FCV_StreamingActivityFCV, ScreenFragment.class, args)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        }, 1000);
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        cameraPermissionRequester = new PermissionRequester(this, Manifest.permission.CAMERA, () -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.FCV_StreamingActivityFCV, CameraFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        });
        cameraPermissionRequester.setTitle(R.string.camera_permission_title);
        cameraPermissionRequester.setExplanation(R.string.camera_permission_explanation);

        audioPermissionRequester = new PermissionRequester(this, Manifest.permission.RECORD_AUDIO, () -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.FCV_StreamingActivityFCV, AudioFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        });
        audioPermissionRequester.setTitle(R.string.record_audio_permission_title);
        audioPermissionRequester.setExplanation(R.string.record_audio_permission_explanation);

        return inflater.inflate(R.layout.activity_streaming_fragment_nav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar appBar = view.findViewById(R.id.TB_Toolbar);
        appBar.setNavigationIcon(R.drawable.icon_back);
        appBar.setTitle(R.string.streaming);
        appBar.setNavigationOnClickListener((v) -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        MaterialButton cameraStreamingB = view.findViewById(R.id.B_StreamCamera);
        MaterialButton screenStreamingB = view.findViewById(R.id.B_StreamScreen);
        MaterialButton audioStreamingB = view.findViewById(R.id.B_StreamAudio);


        cameraStreamingB.setOnClickListener((v) -> {
            cameraPermissionRequester.request();
        });

        screenStreamingB.setOnClickListener((v) -> {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            screenCapturePermissionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
        });

        audioStreamingB.setOnClickListener((v) -> {
            audioPermissionRequester.request();
        });
    }
}
