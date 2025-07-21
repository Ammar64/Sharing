package com.ammar.sharing.network.webrtc;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class CustomSDPObserver implements SdpObserver {
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d("MY_WEBRTC", "Succesful SDP: " + sessionDescription.description);
    }

    @Override
    public void onSetSuccess() {
        Log.d("MY_WEBRTC", "OnSetSuccess() SDP");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e("MY_WEBRTC", "onCreateFailure() Error: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e("MY_WEBRTC", "onCreateFailure() Error: " + s);
    }
}
