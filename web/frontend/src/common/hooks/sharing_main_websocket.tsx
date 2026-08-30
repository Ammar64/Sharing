import React from "react";
import useWebSocket from "react-use-websocket";
import { WebSocketHook } from "react-use-websocket/dist/lib/types";

function __useShareAnyMainWebSocket() {
    return useWebSocket<any>("/api/ws/main", {
        reconnectAttempts: 100,
        reconnectInterval: function(attemptNumber) {
            return Math.min(Math.pow(2, attemptNumber), 20)
        },
        retryOnError: true,
        share: true
    })
}
const WsContext = React.createContext<WebSocketHook<any> | null>(null);

export function MainWebSocketProvider({ children }: { children: React.ReactNode }) {
  const ws = __useShareAnyMainWebSocket();
  return <WsContext.Provider value={ws}>{children}</WsContext.Provider>;
}

export function useShareAnyMainWebSocket() {
  return React.useContext(WsContext);
}

