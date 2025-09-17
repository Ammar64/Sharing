package com.ammar.sharing.services;

import android.content.Intent;
import android.os.Binder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;

import com.ammar.sharing.common.Data;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.webrtc.CustomPeerConnectionObserver;
import com.ammar.sharing.network.webrtc.CustomSDPObserver;
import com.ammar.sharing.network.websocket.sessions.WebRTCSignallingSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
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
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;


public class StreamingService extends LifecycleService {
    private PeerConnectionFactory mPeerConnectionFactory;
    private EglBase mEGLBase;

    @Override
    public void onCreate() {
        super.onCreate();
        mCamera2Emulator = new Camera2Enumerator(this);
        initPeerFactory();
    }


    private static boolean peerConnectionFactoryInitialized = false;
    private void initPeerFactory() {
        if(!peerConnectionFactoryInitialized) {
            PeerConnectionFactory.InitializationOptions initializationOptions
                    = PeerConnectionFactory.InitializationOptions.builder(getApplicationContext())
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
            peerConnectionFactoryInitialized = true;
        }

        mEGLBase = EglBase.create();
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        PeerConnectionFactory.Builder peerConnectionFactoryBuilder = PeerConnectionFactory.builder()
                .setOptions(options);

        EglBase.Context EGLBaseContext = mEGLBase.getEglBaseContext();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                EGLBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(
                EGLBaseContext);

        peerConnectionFactoryBuilder.setVideoEncoderFactory(defaultVideoEncoderFactory);
        peerConnectionFactoryBuilder.setVideoDecoderFactory(defaultVideoDecoderFactory);

        mPeerConnectionFactory = peerConnectionFactoryBuilder.createPeerConnectionFactory();
    }

    @Override
    public void onDestroy() {
        mEGLBase.release();
        if(videoInit) {
            mVideoSource.dispose();
            mSurfaceTextureHelper.dispose();
            mVideoCapturer.dispose();
            mLocalVideoTrack.dispose();
            videoInit = false;
        }
        super.onDestroy();
    }

    private Camera2Enumerator mCamera2Emulator;
    private VideoSource mVideoSource;
    private SurfaceTextureHelper mSurfaceTextureHelper;
    private VideoCapturer mVideoCapturer;
    private VideoTrack mLocalVideoTrack;
    private AudioSource mAudioSource;
    private AudioTrack mAudioTrack;

    public EglBase getEGLBase() {
        return mEGLBase;
    }

    public VideoTrack getLocalVideoTrack() {
        return mLocalVideoTrack;
    }

    private User mUser;

    private boolean videoInit = false;
    public void startStreamingToUser(User user, String sourceDeviceName, int videoWidth, int videoHeight, int fps, boolean useMic) {
        mUser = user;
        if(!videoInit) {
            mVideoSource = mPeerConnectionFactory.createVideoSource(false);
            mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mEGLBase.getEglBaseContext());
            mVideoCapturer = getVideoCapturerFromCameraDeviceName(sourceDeviceName);

            mVideoCapturer.initialize(mSurfaceTextureHelper, this, mVideoSource.getCapturerObserver());
            mVideoCapturer.startCapture(videoWidth, videoHeight, fps);

            mLocalVideoTrack = mPeerConnectionFactory.createVideoTrack("101", mVideoSource);
            mLocalVideoTrack.setEnabled(true);

            if(useMic) {
                mAudioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
                mAudioTrack = mPeerConnectionFactory.createAudioTrack("102", mAudioSource);
                mAudioTrack.setEnabled(true);
            }
            videoInit = true;
        }
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        PeerConnection peerConnection = mPeerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                try {
                    Log.d("MY_WEBRTC", "onIceCandidate sending");
                    JSONObject object = new JSONObject();
                    object.put("action", "webrtc-ice");
                    object.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                    object.put("sdpMid", iceCandidate.sdpMid);
                    object.put("candidate", iceCandidate.sdp);
                    mUser.sendWebSocketMessage(WebRTCSignallingSession.path, object.toString());
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
        peerConnection.addTrack(mLocalVideoTrack, Collections.singletonList("101"));
        if(useMic) {
            peerConnection.addTrack(mAudioTrack, Collections.singletonList("101"));
        }
        Data.startStreamToUser.observe(this ,(a) -> {
            User observedUser = a.t1;
            if(observedUser.getId() == mUser.getId()) {
                boolean firstTime = a.t2;
                if(firstTime) {
                    mUser.setOnWebSocketMessage(WebRTCSignallingSession.path, (data) -> {
                        handleSignalling(peerConnection, data, videoWidth, videoHeight, fps);
                    });
                    mUser.sendWebSocketMessage(WebRTCSignallingSession.path, "{\"action\":\"stream-ready\"}");
                } else {
                    mUser.sendWebSocketMessage(WebRTCSignallingSession.path, "{\"action\":\"renegotiate-me\"}");
                }
            }
        });

        if (!mUser.isWebSocketConnected(WebRTCSignallingSession.path)) {
            return;
        }

        mUser.setOnWebSocketMessage(WebRTCSignallingSession.path, (data) -> {
            handleSignalling(peerConnection, data, videoWidth, videoHeight, fps);
        });
        mUser.sendWebSocketMessage(WebRTCSignallingSession.path, "{\"action\": \"stream-ready\"}");
    }

    private final StreamingServiceBinder mBinder = new StreamingServiceBinder();

    @Nullable
    @Override
    public StreamingServiceBinder onBind(Intent intent) {
        super.onBind(intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mUser != null) {
            mUser.setOnWebSocketMessage(WebRTCSignallingSession.path, null);
        }
        return false;
    }

    public class StreamingServiceBinder extends Binder {
        public StreamingService getService() {
            return StreamingService.this;
        }
    }
    private VideoCapturer getVideoCapturerFromCameraDeviceName(String deviceName) {
        CameraVideoCapturer.CameraEventsHandler cameraEventsHandler = new CameraVideoCapturer.CameraEventsHandler() {
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
        return mCamera2Emulator.createCapturer(deviceName, cameraEventsHandler);
    }
    private void handleSignalling(PeerConnection peerConnection, String data, int width, int height, int fps) {
        try {
            JSONObject object = new JSONObject(data);
            String action = object.getString("action");
            switch (action) {
                case "webrtc-offer":
                    String sdp = object.optString("sdp");
                    String sdptypeStr = object.optString("sdptype");
                    SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdptypeStr), sdp);
                    peerConnection.setRemoteDescription(new CustomSDPObserver(), sessionDescription);
                    negotiate(peerConnection, width, height, fps);
                    break;
                case "webrtc-ice":
                    JSONObject iceCandidateObj = object.optJSONObject("candidate");
                    String sdpMid = "";
                    int sdpMLineIndex = 0;
                    String candidate = "";
                    if (iceCandidateObj != null) {
                        sdpMid = iceCandidateObj.optString("sdpMid");
                        sdpMLineIndex = iceCandidateObj.optInt("sdpMLineIndex");
                        candidate = iceCandidateObj.optString("candidate");
                        if (candidate.isEmpty()) break;
                        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
                        peerConnection.addIceCandidate(iceCandidate);
                    }
                    break;
                case "informing-ready":
                    break;
            }
        } catch (JSONException ignore) {
        }
    }

    private void negotiate(PeerConnection peerConnection, int width, int height, int fps) {
        MediaConstraints sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "false"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(height)));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(width)));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(fps)));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(10)));
        peerConnection.createAnswer(new CustomSDPObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                peerConnection.setLocalDescription(new CustomSDPObserver(), sessionDescription);
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("action", "webrtc-answer");
                    obj.put("sdptype", sessionDescription.type.canonicalForm());
                    obj.put("sdp", sessionDescription.description);
                    mUser.sendWebSocketMessage(WebRTCSignallingSession.path, obj.toString());
                } catch (Exception e) {
                }
            }
        }, sdpConstraints);
    }
}
