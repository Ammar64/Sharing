import i18n from 'i18next'
import { initReactI18next } from 'react-i18next';
import Backend, { HttpBackendOptions } from 'i18next-http-backend';

i18n
    .use(Backend)
    .use(initReactI18next)
    .init<HttpBackendOptions>({
        fallbackLng: "en",
        debug: true,
        backend: {
            loadPath: "/locales/{{lng}}/translations.json"
        },
        interpolation: {
            escapeValue: false, // not needed for react as it escapes by default
        }
    });

window.i18next = i18n;
export default i18n;