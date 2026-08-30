import { Suspense, useEffect, useState } from 'react';
import { CacheProvider, Theme } from '@emotion/react';
import { CssBaseline, ThemeProvider } from '@mui/material';
import createCache from '@emotion/cache';
import { prefixer } from 'stylis';
import rtlPlugin from '@mui/stylis-plugin-rtl';
import { useShareAnyMainWebSocket } from './common/hooks/sharing_main_websocket';
import { createNewShareAnyAppTheme, createShareAnyAppDarkTheme } from './utils/utils';
import { useTranslation } from 'react-i18next';
import { FilesUploadsContext } from './common/contexts';
import type { FileUpload } from './pages/Home/components/UploadProgressDialog';
import { createTheme } from '@mui/material';
import { globalProps, IS_DEBUG } from './consts';
import i18n from './i18n';
import { BrowserRouter, Route, Routes } from 'react-router';
import NotFound from './pages/NotFound/NotFound';
import React from 'react';

const Home = React.lazy(() => import("./pages/Home/Home"));
const Messages = React.lazy(() => import("./pages/Messages/Messages"));
const Stream = React.lazy(() => import("./pages/Stream/Stream"));

export function App() {

  const [theme, setTheme] = useState<Theme>(createShareAnyAppDarkTheme());

  useEffect(() => {
    (async function () {
      const theme = await get_theme();
      setTheme(theme);
    })()
  }, []);

  const { lastJsonMessage } = useShareAnyMainWebSocket();
  useEffect(function () {
    if (lastJsonMessage !== null) {
      if (lastJsonMessage.action === "change-ui") {
        setTheme(createNewShareAnyAppTheme(lastJsonMessage.uiMode, lastJsonMessage.dir));
        document.documentElement.setAttribute("dir", lastJsonMessage.dir);
        i18n.changeLanguage(lastJsonMessage.language);
      }
    }
  }, [lastJsonMessage]);

  const rtlCache = createCache({
    key: 'muirtl',
    stylisPlugins: [prefixer, rtlPlugin],
  });

  const { i18n } = useTranslation();


  const [filesUploadsList, setFilesUploadsList] = useState<FileUpload[]>([]);
  const filesUploadsContextValue = {
    filesUploadsList: filesUploadsList,
    setFilesUploadsList: setFilesUploadsList
  };


  return (
    <CacheProvider value={rtlCache!}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <FilesUploadsContext value={filesUploadsContextValue}>
          <BrowserRouter>
            <Suspense fallback={<div>loading ...</div>}>
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/messages" element={<Messages />} />
                <Route path="/stream" element={<Stream />} />
                <Route path="*" element={<NotFound />} />
              </Routes>
            </Suspense>
          </BrowserRouter>
        </FilesUploadsContext>
      </ThemeProvider>
    </CacheProvider>
  );
}


async function get_theme(): Promise<Theme> {
  let theme;
  if (IS_DEBUG) {
    const TEST_LANG = "ar";
    const TEST_DIR = "rtl";
    const TEST_UIMODE = "dark";

    document.documentElement.setAttribute("dir", TEST_DIR);
    theme = createTheme({
      palette: {
        mode: TEST_UIMODE,
      },
      direction: TEST_DIR,
      components: {
        MuiCssBaseline: {
          styleOverrides: {
            body: {
              background: TEST_UIMODE == "dark" ?
                "linear-gradient(to left, #010c14, #2e012e)" :
                "linear-gradient(to left, #67addf, #df90df)"
            }
          }
        }
      }
    });
    i18n.changeLanguage(TEST_LANG);
  } else {
    const configRes = await fetch("/api/get-app-config");
    const config = await configRes.json();
    globalProps.BROWSER_IP = config.browser_ip;
    i18n.changeLanguage(config.ui_config.language);
    document.documentElement.setAttribute("dir", config.ui_config.direction);
    theme = createNewShareAnyAppTheme(config.ui_config.is_dark_mode, config.ui_config.direction);
  }
  return theme;
}