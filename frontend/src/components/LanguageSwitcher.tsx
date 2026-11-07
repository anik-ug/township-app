import { useTranslation } from 'react-i18next';
import i18n from '../i18n';

export function LanguageSwitcher() {
  const { t } = useTranslation();
  return (
    <select
      aria-label={t('selectLanguage')}
      className="focus-ring rounded-lg border border-slate-300 bg-white px-3 py-2"
      value={i18n.language}
      onChange={(event) => void i18n.changeLanguage(event.target.value)}
    >
      <option value="en">{t('englishLanguage')}</option>
      <option value="hi">{t('hindiLanguage')}</option>
      <option value="mr">{t('marathiLanguage')}</option>
    </select>
  );
}
