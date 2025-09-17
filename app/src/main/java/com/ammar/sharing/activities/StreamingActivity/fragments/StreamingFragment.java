package com.ammar.sharing.activities.StreamingActivity.fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.StreamingActivity.StreamingActivity;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.sessions.WebRTCSignallingSession;
import com.ammar.sharing.services.StreamingService;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import org.webrtc.SurfaceViewRenderer;

public class StreamingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streaming, container, false);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(requireActivity().getWindow(), view);
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            DisplayCutoutCompat displayCutout = windowInsets.getDisplayCutout();
            if (displayCutout != null) {
                int top = displayCutout.getSafeInsetTop();
                Log.d("INSETS", "TOP: " + top);
                view.findViewById(R.id.ABL_AppBarLayout).setPadding(0, top, 0, 0);
            }
            v.setPadding(0, 0, 0, 0);
            return ViewCompat.onApplyWindowInsets(v, windowInsets);
        });

        Bundle args = getArguments();
        assert args != null;
        mStreamWidth = args.getInt("width");
        mStreamHeight = args.getInt("height");
        mStreamFPS = args.getInt("fps");
        mUserIndex = args.getInt("userIndex");
        mUseMic = args.getBoolean("mic");
        mVideoSourceDeviceName = args.getString("sourceDeviceName");

        return view;
    }


    private MaterialToolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private SurfaceViewRenderer mRemoteStreamView;
    private SurfaceViewRenderer mLocalStreamView;
    Handler handler = new Handler(Looper.getMainLooper());
    private boolean mToolbarShown = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAppBarLayout = view.findViewById(R.id.ABL_AppBarLayout);
        hideToolbarLater();

        ((StreamingActivity) requireActivity()).setTouchUpListener(() -> {
            Log.d("TOUCH_TEST", "TOUCH_UP");
            showToolbar();
        });

        mToolbar = view.findViewById(R.id.MTB_StreamingToolbar);
        mToolbar.setNavigationOnClickListener((v) -> {
            Navigation.findNavController(view).navigateUp();
        });


        mRemoteStreamView = view.findViewById(R.id.SVR_RemoteStreamView);
        mLocalStreamView = view.findViewById(R.id.SVR_LocalStreamView);
    }

    private StreamingService mStreamingService;
    private boolean mStreamingServiceBound = false;

    private int mStreamWidth;
    private int mStreamHeight;
    private int mStreamFPS;
    private boolean mUseMic;
    private int mUserIndex;
    private String mVideoSourceDeviceName;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            var binder = (StreamingService.StreamingServiceBinder) service;
            mStreamingService = binder.getService();
            mStreamingServiceBound = true;
            mStreamingService.startStreamingToUser(User.users.get(mUserIndex), mVideoSourceDeviceName, mStreamWidth, mStreamHeight, mStreamFPS, mUseMic);

            mLocalStreamView.init(mStreamingService.getEGLBase().getEglBaseContext(), null);
            mLocalStreamView.setMirror(false);
            mLocalStreamView.setZOrderMediaOverlay(false);
            mStreamingService.getLocalVideoTrack().addSink(mLocalStreamView);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mStreamingServiceBound = false;
        }
    };


    private boolean firstStart = true;
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), StreamingService.class);
        requireContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);

        if(!firstStart) {
            User.users.get(mUserIndex).sendWebSocketMessage(WebRTCSignallingSession.path, "{\"action\":\"renegotiate-me\"}");
        }
        firstStart = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocalStreamView.release();
        requireContext().unbindService(mServiceConnection);
        mStreamingServiceBound = false;
    }

    @Override
    public void onDestroyView() {
        cancelToolbarHideTask();
        ((StreamingActivity) requireActivity()).setTouchUpListener(null);
        super.onDestroyView();
    }

    private void hideToolbar() {
        if (mToolbarShown) {
            mAppBarLayout.animate()
                    .translationY(-mAppBarLayout.getMeasuredHeight())
                    .start();
            mToolbarShown = false;
        }
    }

    private void showToolbar() {
        if (!mToolbarShown) {
            mAppBarLayout.animate()
                    .translationY(0)
                    .start();
            mToolbarShown = true;
            hideToolbarLater();
        } else {
            cancelToolbarHideTask();
            hideToolbarLater();
        }
    }

    private void hideToolbarLater() {
        handler.postDelayed(this::hideToolbar, 3000);
    }

    private void cancelToolbarHideTask() {
        handler.removeCallbacksAndMessages(null);
    }

}
