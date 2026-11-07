import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import i18n, { i18nReady } from './i18n';
import { App } from './App';
import './styles.css';

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => void navigator.serviceWorker.register('/sw.js'));
}

void i18nReady.then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <App />
    </StrictMode>,
  );
});
