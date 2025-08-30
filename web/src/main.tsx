import { createRoot } from 'react-dom/client';
import { App } from './App';
import { createTheme } from '@mui/material';
import { IS_DEBUG } from './consts';
import i18n from './i18n';

(async function () {
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
        const configRes = await fetch("/get-app-config");
        const config = await configRes.json();
        i18n.changeLanguage(config.language);
        document.documentElement.setAttribute("dir", config.dir);
        theme = createTheme({
            palette: {
                mode: config.uiMode
            },
            direction: config.dir,
            components: {
                MuiCssBaseline: {
                    styleOverrides: {
                        body: {
                            background: config.uiMode === "light" ?
                                "linear-gradient(to left, #67addf, #df90df)" :
                                "linear-gradient(to left, #010c14, #2e012e)"
                        }
                    }
                }
            }
        });
    }

    let container = document.getElementById("app")!;
    let root = createRoot(container);
    root.render(
        <App theme={theme} />
    );
})();

