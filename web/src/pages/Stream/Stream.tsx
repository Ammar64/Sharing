import { Box, Button, CardMedia } from "@mui/material";
import useWebRTCSignallingWebSocket from "common/hooks/web_rtc_websocket";
import { RefObject, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";

function Stream() {
    const { t } = useTranslation();
    const playButtonRef = useRef<HTMLButtonElement>(null);
    const remoteCardMediaRef = useRef<HTMLVideoElement>(null);
    useRTCPeerConnection(remoteCardMediaRef, playButtonRef);
    return (
        <Box
            sx={{
                height: "100dvh",
                width: "100dvw",
                display: "flex",
                justifyContent: "center",
                alignItems: "center"
            }} >
            <CardMedia
                component="video"
                ref={remoteCardMediaRef}
                sx={{ height: "100%" }} />

            <Button
                ref={playButtonRef}
                variant="contained"
                color="secondary"
                sx={{
                    position: "absolute",
                    top: "50%",
                    left: "50%",
                    transform: "translate(-50%,-50%)",
                    display: "none"
                }}
                onClick={function () {
                    remoteCardMediaRef.current?.play();
                    playButtonRef.current!.style.display = "none";
                }}
            >{t("tap_to_play_stream")}</Button>
        </Box>
    );
}


function useRTCPeerConnection(remoteCardMediaRef: RefObject<HTMLVideoElement | null>, playButtonRef: RefObject<HTMLButtonElement | null>) {
    const { lastJsonMessage, sendJsonMessage } = useWebRTCSignallingWebSocket();


    const peerConnectionRef = useRef<RTCPeerConnection>(new RTCPeerConnection());
    const [streamingReady, setStreamingReady] = useState(false);
    const peerConn = peerConnectionRef.current;


    function negotiate() {
        if (streamingReady) {
            (async function () {
                const offer = await peerConn.createOffer({ offerToReceiveVideo: true, offerToReceiveAudio: true });
                await peerConn.setLocalDescription(offer);
                const offerObj = {
                    action: "webrtc-offer",
                    sdp: offer.sdp,
                    sdptype: offer.type
                };
                sendJsonMessage(offerObj);
            })();
        } else {
            sendJsonMessage({ action: "ready-to-receive", firstTime: true });
        }
    }

    useEffect(function () {
        (async function () {
            if (lastJsonMessage == null) return;
            switch (lastJsonMessage.action) {
                case "webrtc-answer":
                    const rtcSessionDescription = new RTCSessionDescription({ sdp: lastJsonMessage.sdp, type: lastJsonMessage.sdptype });
                    await peerConn.setRemoteDescription(rtcSessionDescription);
                    break;
                case "webrtc-ice":
                    const iceCandidate = new RTCIceCandidate({
                        candidate: lastJsonMessage.candidate,
                        sdpMid: lastJsonMessage.sdpMid,
                        sdpMLineIndex: lastJsonMessage.sdpMLineIndex,
                    });
                    peerConn.addIceCandidate(iceCandidate);
                    break;
                case "stream-ready":
                    setStreamingReady(true);
                    break;
                case "renegotiate-me":
                    negotiate();
                    break;
            }
        })();
    }, [lastJsonMessage]);

    useEffect(function () {
        peerConn.onicecandidate = function (event) {
            const candidate = event.candidate;
            if (candidate !== null) {
                const iceCandidateMessage = {
                    action: "webrtc-ice",
                    candidate: candidate
                };
                sendJsonMessage(iceCandidateMessage);
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
            console.log(event.streams);
            remoteCardMediaRef.current!.srcObject = event.streams[0];
            remoteCardMediaRef.current!.onerror = function (e) {
                console.log("ERROR: " + e.toString());
            }
            remoteCardMediaRef.current!.play()
                .catch(function () {
                    playButtonRef.current!.style.display = "block";
                });
        };
        negotiate();
    }, [streamingReady]);

}

export default Stream;