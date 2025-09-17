package com.ammar.sharing.activities.StreamingActivity.fragments;

import android.Manifest;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.StreamingActivity.adapterR.UserSelectAdapter;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.custom.ui.PermissionRequester;
import com.ammar.sharing.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import org.webrtc.Camera2Enumerator;

import java.util.Locale;

public class StreamSetupFragment extends Fragment {
    Camera2Enumerator mCamera2Enumerator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCamera2Enumerator = new Camera2Enumerator(requireContext());
        View view = inflater.inflate(R.layout.fragment_streaming_setup, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return ViewCompat.onApplyWindowInsets(v, windowInsets);
        });

        return view;
    }

    AutoCompleteTextView mVideoSourceInput;
    AutoCompleteTextView mVideoResolutionInput;
    Slider mFPSInput;
    CheckBox mMicrophoneCB;
    RecyclerView mUserSelectRV;
    TextView mNoUserConnectedTV;
    MaterialButton mContinueButton;

    PermissionRequester mCameraPermissionRequester;
    PermissionRequester mMicrophonePermissionRequester;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCameraPermissionRequester = new PermissionRequester(this, Manifest.permission.CAMERA);
        mMicrophonePermissionRequester = new PermissionRequester(this, Manifest.permission.RECORD_AUDIO);
        Toolbar toolbar = view.findViewById(R.id.TB_Toolbar);
        toolbar.setTitle(R.string.streaming);

        mVideoSourceInput = view.findViewById(R.id.ACTV_VideoSource);
        mVideoResolutionInput = view.findViewById(R.id.ACTV_VideoRes);
        mFPSInput = view.findViewById(R.id.S_Framerate);
        mMicrophoneCB = view.findViewById(R.id.MCB_AllowMic);
        mUserSelectRV = view.findViewById(R.id.RV_UserSelect);
        mNoUserConnectedTV = view.findViewById(R.id.TV_NoUserConnected);
        mContinueButton = view.findViewById(R.id.B_Continue);

        VideoSource[] videoSources = getCameraNames();
        String[] cameraNamesStr = new String[videoSources.length];
        for (int i = 0; i < videoSources.length; i++) {
            cameraNamesStr[i] = videoSources[i].name;
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cameraNamesStr);
        mVideoSourceInput.setAdapter(arrayAdapter);
        mVideoSourceInput.setOnItemClickListener((parent, v, pos, id) -> {
            VideoSource videoSource = videoSources[pos];
            if (videoSource.isCamera) {
                var formats = mCamera2Enumerator.getSupportedFormats(videoSource.id);
                assert formats != null;
                VideoSourceProps[] props = new VideoSourceProps[formats.size()];
                for (int i = 0; i < formats.size(); i++) {
                    var f = formats.get(i);
                    props[i] = new VideoSourceProps(
                            f.width,
                            f.height,
                            f.framerate.min,
                            f.framerate.max
                    );
                }
                mVideoSourceSelected = true;
                handleContinueButtonActivation();
                setupVideoResOptions(videoSource, props);
            }
        });

        UserSelectAdapter adapter = new UserSelectAdapter();
        adapter.setOnSelectedListener((index) -> {
            mUserIndex = index;
            mUserSelected = true;
            handleContinueButtonActivation();
        });
        mUserSelectRV.setAdapter(adapter);
        Data.usersListObserver.observe(getViewLifecycleOwner(), info -> {
            char action = info.getChar("action");
            int index = info.getInt("index");
            if ('A' == action) {
                adapter.notifyItemInserted(index);
            } else if ('C' == action) {
                adapter.notifyItemChanged(index);
            }

            int size = User.users.size();
            if (size == 0) {
                mNoUserConnectedTV.setVisibility(View.VISIBLE);
            } else {
                mNoUserConnectedTV.setVisibility(View.GONE);
            }

        });
    }

    private void setupVideoResOptions(VideoSource source, VideoSourceProps[] props) {
        mVideoResolutionInput.setEnabled(true);
        String[] videoResLabels = new String[props.length];
        for (int i = 0; i < props.length; i++) {
            VideoSourceProps prop = props[i];
            videoResLabels[i] = String.format(Locale.ENGLISH,
                    "%dx%d",
                    prop.width,
                    prop.height
            );
        }
        ArrayAdapter<String> videoResArrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, videoResLabels);
        if (videoResLabels.length > 0) {
            mVideoResolutionInput.setText(videoResLabels[0]);
            mVideoResSelected = true;
            handleContinueButtonActivation();
        }
        mVideoResolutionInput.setAdapter(videoResArrayAdapter);
        mContinueButton.setOnClickListener((v) -> mCameraPermissionRequester.request(() -> {
            String resText = mVideoResolutionInput.getText().toString();
            String[] resSplit = resText.split("x");
            int width = Integer.parseInt(resSplit[0]);
            int height = Integer.parseInt(resSplit[1]);
            Size res = new Size(width, height);

            boolean needsMic = mMicrophoneCB.isChecked();
            Bundle args = new Bundle();
            args.putInt("width", res.getWidth());
            args.putInt("height", res.getHeight());
            args.putInt("fps", Math.round(mFPSInput.getValue()));
            args.putInt("userIndex", mUserIndex);
            args.putBoolean("mic", needsMic);
            args.putString("sourceDeviceName", source.id);

            if(needsMic) {
                mMicrophonePermissionRequester.request(() -> Navigation.findNavController(requireView()).navigate(R.id.action_streamSetupFragment_to_streamingFragment, args));
            } else {
                Navigation.findNavController(requireView()).navigate(R.id.action_streamSetupFragment_to_streamingFragment, args);
            }
        }));
    }

    private VideoSource[] getCameraNames() {
        String[] cameras = mCamera2Enumerator.getDeviceNames();
        VideoSource[] videoSources = new VideoSource[cameras.length];
        for (int i = 0; i < cameras.length; i++) {
            String name;
            if ("0".equals(cameras[i])) {
                name = getString(R.string.rear_camera);
            } else if ("1".equals(cameras[i])) {
                name = getString(R.string.front_camera);
            } else {
                name = "Camera  " + cameras[i];
            }
            videoSources[i] = new VideoSource();
            videoSources[i].id = cameras[i];
            videoSources[i].name = name;
            videoSources[i].isCamera = true;
        }
        return videoSources;
    }


    private int mUserIndex = -1;
    private boolean mVideoSourceSelected = false;
    private boolean mVideoResSelected = false;
    private boolean mUserSelected = false;

    private void handleContinueButtonActivation() {
        mContinueButton.setEnabled(mVideoSourceSelected && mVideoResSelected && mUserSelected);
    }
}
