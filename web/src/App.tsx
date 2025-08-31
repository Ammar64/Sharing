import { BrowserRouter, Route, Routes } from 'react-router';
import Home from './pages/Home/Home';
import NotFound from './pages/NotFound/NotFound';
import { useEffect, useState } from 'react';
import { CacheProvider } from '@emotion/react';
import { CssBaseline, ThemeProvider } from '@mui/material';
import createCache from '@emotion/cache';
import { prefixer } from 'stylis';
import rtlPlugin from '@mui/stylis-plugin-rtl';
import useSharingMainWebSocket from 'common/hooks/sharing_main_websocket';
import { createNewSharingAppTheme } from 'utils/utils';
import { useTranslation } from 'react-i18next';
import Messages from 'pages/Messages/Messages';

export function App(props: any) {
  const rtlCache = createCache({
    key: 'muirtl',
    stylisPlugins: [prefixer, rtlPlugin],
  });

  const { i18n } = useTranslation();

  const [theme, setTheme] = useState(props.theme);
  const { lastJsonMessage } = useSharingMainWebSocket();
  useEffect(function () {
    if (lastJsonMessage !== null) {
      if (lastJsonMessage.action === "change-ui") {
        setTheme(createNewSharingAppTheme(lastJsonMessage.uiMode, lastJsonMessage.dir));
        document.documentElement.setAttribute("dir", lastJsonMessage.dir);
        i18n.changeLanguage(lastJsonMessage.language);
      }
    }
  }, [lastJsonMessage]);

  return (
      <CacheProvider value={rtlCache!}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <BrowserRouter>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/messages" element={<Messages />} />
              <Route path="*" element={<NotFound />} />
            </Routes>
          </BrowserRouter>
        </ThemeProvider>
      </CacheProvider>
  );
}
