package com.ammar.sharing.activities.StreamingActivity.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
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
import org.webrtc.AudioProcessingFactory;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.BuiltinAudioDecoderFactoryFactory;
import org.webrtc.BuiltinAudioEncoderFactoryFactory;
import org.webrtc.EglBase;
import org.webrtc.ExternalAudioProcessingFactory;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.voiceengine.WebRtcAudioManager;

import java.util.ArrayList;
import java.util.Collections;

public class AudioFragment extends Fragment {
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
        mUser.sendWebSocketMessage(InfoWSSession.path, "{\"type\": \"go-to-audio-page\"}");

        initWebRTC();
        Data.webRTCStartOffer.observe(getViewLifecycleOwner(), (ignore) -> {
            handlePeerConnection();
        });
    }

    private EglBase.Context mEGLBaseContext;
    private PeerConnectionFactory mPeerConnectionFactory;
    private PeerConnection mPeerConnection;
    private AudioSource mAudioSource;
    private AudioTrack mLocalAudioTrack;

    private void initWebRTC() {
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

        BuiltinAudioEncoderFactoryFactory builtinAudioEncoderFactoryFactory = new BuiltinAudioEncoderFactoryFactory();
        BuiltinAudioDecoderFactoryFactory builtinAudioDecoderFactoryFactory = new BuiltinAudioDecoderFactoryFactory();

        peerConnectionFactoryBuilder.setAudioEncoderFactoryFactory(builtinAudioEncoderFactoryFactory);
        peerConnectionFactoryBuilder.setAudioDecoderFactoryFactory(builtinAudioDecoderFactoryFactory);

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

        mAudioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mLocalAudioTrack = mPeerConnectionFactory.createAudioTrack("102", mAudioSource);
        mLocalAudioTrack.setEnabled(true);
        mPeerConnection.addTrack(mLocalAudioTrack, Collections.singletonList("102"));
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

    @Override
    public void onDestroyView() {
        if( mAudioSource != null ) {
            mAudioSource.dispose();
        }
        if (mLocalAudioTrack != null) {
            mLocalAudioTrack.setEnabled(false);
            mLocalAudioTrack.dispose();
        }
        mPeerConnection.close();
        mPeerConnectionFactory.dispose();
        super.onDestroyView();
    }
}
