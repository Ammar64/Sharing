import { createRoot } from 'react-dom/client';
import { StrictMode } from 'react';
import { App } from './App';

import "./i18n"
import { BrowserRouter } from 'react-router';

let container = document.getElementById("app")!;
let root = createRoot(container)
root.render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
