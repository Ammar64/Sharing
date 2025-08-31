import type { Message } from "pages/Messages/Messages";
import useWebSocket from "react-use-websocket";

function useMessagesWebsocket() {
    return useWebSocket<Message>("/messages/ws", {
        reconnectAttempts: 100,
        reconnectInterval: function (attemptNumber) {
            return Math.min(Math.pow(2, attemptNumber), 20)
        },
        retryOnError: true,
        share: true
    })
}
export default useMessagesWebsocket;