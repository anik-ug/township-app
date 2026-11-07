import { useTranslation } from 'react-i18next';
import i18n from '../i18n';

export const languageOptions = [
  { code: 'en', label: 'English' },
  { code: 'hi', label: 'हिन्दी' },
  { code: 'mr', label: 'मराठी' },
] as const;

export function LanguageSwitcher() {
  const { t } = useTranslation();
  return (
    <select
      aria-label={t('selectLanguage')}
      className="focus-ring rounded-lg border border-slate-300 bg-white px-3 py-2"
      value={i18n.language}
      onChange={(event) => void i18n.changeLanguage(event.target.value)}
    >
      {languageOptions.map(({ code, label }) => (
        <option key={code} value={code} lang={code === 'en' ? undefined : code}>
          {label}
        </option>
      ))}
    </select>
  );
}
