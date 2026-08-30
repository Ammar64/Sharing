import { createRoot } from 'react-dom/client';
import { App } from './App';
import React from 'react';
import { MainWebSocketProvider } from './common/hooks/sharing_main_websocket';


let container = document.getElementById("app")!;
let root = createRoot(container);
root.render(
    <MainWebSocketProvider>
        <App />
    </MainWebSocketProvider>
);