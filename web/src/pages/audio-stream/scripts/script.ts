import { RTCOffer } from "../../../common/scripts/interfaces";
function main() {
    const audioButton = document.getElementById("play-audio-button") as HTMLButtonElement;
    audioButton.textContent = "play audio"

    const audio = new Audio();

    const infoWS = new WebSocket("/info");
    let peerConn: RTCPeerConnection;
    
    audioButton.onclick = function() {
        if( audio.paused ) {
            audio.play();
            audioButton.textContent = "stop audio"
        } else {
            audio.pause();
            audioButton.textContent = "play audio"
        }
    }

    infoWS.addEventListener("message", function (event) {
        const data = JSON.parse(event.data);
        const type = data.type;
        if (type === "webrtc-offer") {
            const rtcOffer = data as RTCOffer;
            handleRTCOfferMsg(rtcOffer);
        } else if (type === "webrtc-ice-candidate") {
            peerConn.addIceCandidate(new RTCIceCandidate({ candidate: data.candidate, sdpMid: data.id, sdpMLineIndex: data.label }));
        } else if(type === "go-to-audio-page") {
            infoWS.send(JSON.stringify({ type: "informing-ready" }));
        }
    })
    infoWS.addEventListener("open", function (e) {
        infoWS.send(JSON.stringify({ type: "informing-ready" }));
    })
    /*  functions */
    function createPeerConnection() {
        peerConn = new RTCPeerConnection();
        peerConn.onicecandidate = function (event) {
            const candidate = event.candidate;
            if (candidate !== null) {
                const iceCandidateMessage = {
                    type: "webrtc-ice-candidate",
                    candidate: candidate
                };
                infoWS.send(JSON.stringify(iceCandidateMessage));
            }
        };

        peerConn.onnegotiationneeded = function (e) {
            console.log("Negotiation needed ");
        };

        peerConn.onconnectionstatechange = function (e) {
            const connection = e.target as RTCPeerConnection;
            console.log("onconnectionstatechange " + connection.connectionState);
        };

        peerConn.onicecandidateerror = function (e) {
            console.log("onicecandidateerror. Error code " + e.errorText + ". Error: " + e.errorText);
        }

        peerConn.onsignalingstatechange = function (e) {
            const connection = e.target as RTCPeerConnection;
            console.log("onsignalingstatechange " + connection.signalingState);
        }

        peerConn.onicegatheringstatechange = function (e) {
            const connection = e.target as RTCPeerConnection;
            console.log("onicegatheringstatechange " + connection.iceGatheringState);
            
        }

        peerConn.oniceconnectionstatechange = function (e) {
            const connection = e.target as RTCPeerConnection;
            console.log("oniceconnectionstatechange " + connection.iceConnectionState);
        }

        peerConn.ondatachannel = function (e) {
            console.log("ondatachannel");
        }

        peerConn.ontrack = function (event) {
            console.log("ontrack is called");
            audio.srcObject = event.streams[0];
        };
    }

    async function handleRTCOfferMsg(offer: RTCOffer) {
        try {
            createPeerConnection();
            const desc = new RTCSessionDescription({ sdp: offer.sdp, type: offer.sdptype as any });
            await peerConn.setRemoteDescription(desc);
            const answer = await peerConn.createAnswer({ 'mandatory': { 'OfferToReceiveAudio': true, 'OfferToReceiveVideo': false } });
            await peerConn.setLocalDescription(answer);
            const offerRes = {
                type: "webrtc-answer",
                sdp: peerConn.localDescription?.sdp,
                sdptype: peerConn.localDescription?.type
            };
            infoWS.send(JSON.stringify(offerRes));
        } catch (e) {
            alert("Error handleRTCOfferMsg(): " + e)
        }
    }
}
main();