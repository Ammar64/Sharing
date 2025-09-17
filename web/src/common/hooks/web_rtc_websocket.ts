import useWebSocket from "react-use-websocket";

export default function useWebRTCSignallingWebSocket() {
    return useWebSocket<{action: string, [key: string]: any}>("/web-rtc", {
        reconnectAttempts: 100,
        reconnectInterval: function(attemptNumber) {
            return Math.min(Math.pow(2, attemptNumber), 20)
        },
        retryOnError: true,
        share: true
    })
}