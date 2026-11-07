import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import en from './locales/en.json';
import hi from './locales/hi.json';
import mr from './locales/mr.json';

const savedLanguage = localStorage.getItem('township_language') || 'en';

export const i18nReady = i18n
  .use(initReactI18next)
  .init({
    resources: { en: { translation: en }, hi: { translation: hi }, mr: { translation: mr } },
    lng: savedLanguage,
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
  })
  .then(() => {
    document.documentElement.lang = i18n.language;
    document.title = i18n.t('appName');
  });

i18n.on('languageChanged', (language) => {
  localStorage.setItem('township_language', language);
  document.documentElement.lang = language;
  document.title = i18n.t('appName');
});

export default i18n;
