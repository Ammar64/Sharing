package com.ammar.sharing.network.websocket.sessions;

import com.ammar.sharing.common.Data;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class InfoWSSession extends WebSocketSession {
    public InfoWSSession(User user) {
        super(user);
    }

    @Override
    public void onMessage(WebSocket socket, String data) {
        try {
            JSONObject object = new JSONObject(data);
            String type = object.getString("type");
            switch (type) {
                case "webrtc-answer":
                    String sdp = object.optString("sdp");
                    String sdptypeStr = object.optString("sdptype");
                    SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdptypeStr), sdp);
                    Data.webRTCSessionDescription.forcePostValue(sessionDescription);
                    break;
                case "webrtc-ice-candidate":
                    JSONObject iceCandidateObj = object.optJSONObject("candidate");
                    String sdpMid = "";
                    int sdpMLineIndex = 0;
                    String candidate = "";
                    if( iceCandidateObj != null ) {
                        sdpMid = iceCandidateObj.optString("sdpMid");
                        sdpMLineIndex = iceCandidateObj.optInt("sdpMLineIndex");
                        candidate = iceCandidateObj.optString("candidate");
                        if(candidate.isEmpty()) break;
                        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
                        Data.webRTCIceCandidate.forcePostValue(iceCandidate);
                    }
                    break;
                case "informing-ready":
                    Data.webRTCStartOffer.forcePostValue(null);
                    break;
            }
        } catch (JSONException e) {
        }

    }

    public static final String path = "/info";
}
