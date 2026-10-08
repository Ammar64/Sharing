import useWebSocket from "react-use-websocket";

export default function useSharingMainWebSocket() {
    return useWebSocket<{action: string, [key: string]: any}>("/ws", {
        reconnectAttempts: 100,
        reconnectInterval: function(attemptNumber) {
            return Math.min(Math.pow(2, attemptNumber), 20)
        },
        retryOnError: true,
        share: true
    })
}