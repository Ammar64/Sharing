package com.ammar.sharing.activities.StreamingActivity.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.webrtc.CustomPeerConnectionObserver;
import com.ammar.sharing.network.webrtc.CustomSDPObserver;
import com.ammar.sharing.network.websocket.sessions.InfoWSSession;

import org.json.JSONObject;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;

public class CameraFragment extends Fragment {
    User mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_streaming_fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mEGLBaseContext = EglBase.create().getEglBaseContext();
        mUser = User.users.get(0); // TODO: Select user
        mUser.sendWebSocketMessage(InfoWSSession.path, "{\"type\": \"go-to-video-page\"}");
        mLocalVideoView = view.findViewById(R.id.SVR_CameraSurface);

        initWebRTC(mLocalVideoView);
        Data.webRTCStartOffer.observe(getViewLifecycleOwner(), (ignore) -> {
            handlePeerConnection();
        });
    }

    EglBase.Context mEGLBaseContext;
    PeerConnectionFactory mPeerConnectionFactory;
    PeerConnection mPeerConnection;
    private VideoTrack mLocalVideoTrack;
    private SurfaceViewRenderer mLocalVideoView;
    private VideoCapturer mVideoCapturer;

    private void initWebRTC(SurfaceViewRenderer localVideoView) {
        PeerConnectionFactory.InitializationOptions initializationOptions
                = PeerConnectionFactory.InitializationOptions.builder(requireContext().getApplicationContext())
                .setEnableInternalTracer(true)
                .setInjectableLogger((message, severity, tag) -> {
                    int p = switch (severity) {
                        case LS_INFO -> Log.INFO;
                        case LS_VERBOSE -> Log.VERBOSE;
                        case LS_WARNING -> Log.WARN;
                        case LS_ERROR -> Log.ERROR;
                        case LS_NONE -> Log.DEBUG;
                    };
                    Log.println(p, "WEBRTC_INJ." + tag, message);

                }, Logging.Severity.LS_VERBOSE)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        PeerConnectionFactory.Builder peerConnectionFactoryBuilder = PeerConnectionFactory.builder()
                .setOptions(options);

        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                mEGLBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(
                mEGLBaseContext);

        peerConnectionFactoryBuilder.setVideoEncoderFactory(defaultVideoEncoderFactory);
        peerConnectionFactoryBuilder.setVideoDecoderFactory(defaultVideoDecoderFactory);

        mPeerConnectionFactory = peerConnectionFactoryBuilder.createPeerConnectionFactory();

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        mPeerConnection = mPeerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver() {

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                try {

                    Log.d("MY_WEBRTC", "onIceCandidate sending");
                    JSONObject object = new JSONObject();
                    object.put("type", "webrtc-ice-candidate");
                    object.put("label", iceCandidate.sdpMLineIndex);
                    object.put("id", iceCandidate.sdpMid);
                    object.put("candidate", iceCandidate.sdp);
                    // TODO: Allow user to select users
                    mUser.sendWebSocketMessage(InfoWSSession.path, object.toString());

                } catch (Exception e) {
                    Log.e("MY_WEBRTC", "onIceCandidate(). Error: " + e);
                }
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d("MY_WEBRTC", "onRenegotiationNeeded");
                //negotiate();
            }
        });

        Data.webRTCIceCandidate.observe(getViewLifecycleOwner(), (v) -> {
            mPeerConnection.addIceCandidate(v);
        });
        Data.webRTCSessionDescription.observe(getViewLifecycleOwner(), (v) -> {
            mPeerConnection.setRemoteDescription(new CustomSDPObserver(), v);
        });

        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mEGLBaseContext);
        mVideoCapturer = createCameraCapturer(new Camera2Enumerator(requireContext()));
        if (mVideoCapturer == null) {
            Log.d("MYTAG", "mVideoCapturer is null");
        }
        mVideoCapturer.initialize(surfaceTextureHelper, requireContext(), videoSource.getCapturerObserver());
        mVideoCapturer.startCapture(1280, 720, 45);

        mLocalVideoTrack = mPeerConnectionFactory.createVideoTrack("101", videoSource);
        mLocalVideoTrack.setEnabled(true);

        mPeerConnection.addTrack(mLocalVideoTrack, Collections.singletonList("101"));
        localVideoView.setMirror(false);
        localVideoView.init(mEGLBaseContext, null);// Attach the video track to the renderer.
        localVideoView.setZOrderMediaOverlay(false);
        mLocalVideoTrack.addSink(localVideoView);
    }

    private void handlePeerConnection() {
        negotiate();
    }

    private void negotiate() {
        MediaConstraints sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "false"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(720)));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(1280)));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(60)));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(30)));
        mPeerConnection.createOffer(new CustomSDPObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                mPeerConnection.setLocalDescription(new CustomSDPObserver(), sessionDescription);
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "webrtc-offer");
                    obj.put("sdptype", sessionDescription.type.canonicalForm());
                    obj.put("sdp", sessionDescription.description);
                    mUser.sendWebSocketMessage(InfoWSSession.path, obj.toString());
                } catch (Exception e) {
                }
            }
        }, sdpConstraints);
    }

    private void renegotiate() {
//        MediaConstraints sdpConstraints = new MediaConstraints();
//        peerConnection.createOffer(new CustomSDPObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {
//                super.onCreateSuccess(sessionDescription);
//                if( peerConnection.signalingState() != PeerConnection.SignalingState.STABLE ) return;
//                peerConnection.setLocalDescription(new CustomSDPObserver(), sessionDescription);
//                try {
//                    JSONObject obj = new JSONObject();
//                    obj.put("type", "webrtc-offer");
//                    obj.put("sdptype", sessionDescription.type.canonicalForm());
//                    obj.put("sdp", sessionDescription.description);
//                    user.sendWebSocketMessage(InfoWSSession.path, obj.toString());
//                } catch (Exception e) {
//                }
//            }
//        }, sdpConstraints);

    }


    private VideoCapturer createCameraCapturer(Camera2Enumerator camera2Enumerator) {
        String[] deviceName = camera2Enumerator.getDeviceNames();
        for (String i : deviceName) {
            if (camera2Enumerator.isBackFacing(i)) {
                return camera2Enumerator.createCapturer(i, mCameraEventsHandler);
            }
        }
        // if no back facing camera return the first one.
        return camera2Enumerator.createCapturer(deviceName[0], mCameraEventsHandler);
    }

    CameraVideoCapturer.CameraEventsHandler mCameraEventsHandler = new CameraVideoCapturer.CameraEventsHandler() {
        @Override
        public void onCameraError(String s) {
            Log.e("CAMERA_WEBRTC", "CameraError: " + s);
        }

        @Override
        public void onCameraDisconnected() {
            Log.d("CAMERA_WEBRTC", "onCameraDisconnected");
        }

        @Override
        public void onCameraFreezed(String s) {
            Log.d("CAMERA_WEBRTC", "onCameraFreezed: " + s);
        }

        @Override
        public void onCameraOpening(String s) {
            Log.d("CAMERA_WEBRTC", "onCameraOpening: " + s);

        }

        @Override
        public void onFirstFrameAvailable() {
            Log.d("CAMERA_WEBRTC", "onFirstFrameAvailable: ");
        }

        @Override
        public void onCameraClosed() {
            Log.d("CAMERA_WEBRTC", "onCameraClosed: ");
        }
    };


    @Override
    public void onDestroyView() {
        if (mLocalVideoTrack != null) {
            mLocalVideoTrack.setEnabled(false);
        }
        if (mVideoCapturer != null) {
            try {
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                Log.i("MY_WEBRTC", "mVideoCapturer InterruptedException");
            }
        }
        mLocalVideoView.release();
        mPeerConnection.close();
        mPeerConnectionFactory.dispose();
        super.onDestroyView();
    }
}
