import { useTranslation } from 'react-i18next';
import { clearSession } from '../../lib/session';
import { Button } from '../../components/Button';

export function WrongRole() {
  const { t } = useTranslation();
  return (
    <div className="mx-auto max-w-md px-4 py-16 text-center">
      <h1 className="mb-3 text-2xl font-bold">{t('thisAppResidents')}</h1>
      <p className="mb-6 text-muted">{t('wrongRole')}</p>
      <Button
        onClick={() => {
          clearSession();
          window.location.assign('/login');
        }}
      >
        {t('logout')}
      </Button>
    </div>
  );
}
