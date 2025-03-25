package com.ammar.sharing.network.webrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

public class CustomPeerConnectionObserver implements PeerConnection.Observer {
    @Override
    public void onSignalingChange(PeerConnection.SignalingState newState) {
        Log.d("MY_WEBRTC", "onSignalingChange(). " + newState.name());
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
        Log.d("MY_WEBRTC", "onIceConnectionChange(). " + newState.name());
    }

    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
        Log.d("MY_WEBRTC", "onIceConnectionReceivingChange(). " + receiving);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
        Log.d("MY_WEBRTC", "onIceGatheringChange(). " + newState.name());
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        Log.d("MY_WEBRTC", "onIceCandidate(). SDP: " + candidate.sdp);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        Log.d("MY_WEBRTC", "onIceCandidatesRemoved().");
    }

    @Override
    public void onAddStream(MediaStream stream) {
        Log.d("MY_WEBRTC", "onAddStream().");
    }

    @Override
    public void onRemoveStream(MediaStream stream) {
        Log.d("MY_WEBRTC", "onRemoveStream().");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d("MY_WEBRTC", "onDataChannel(). label: " + dataChannel.label());
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d("MY_WEBRTC", "onRenegotiationNeeded().");
    }


}
