import { useTranslation } from 'react-i18next';
import { clearSession } from '../../lib/session';
import { getUser } from '../../lib/session';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { LanguageSwitcher } from '../../components/LanguageSwitcher';
import { PageHeader } from '../../components/PageHeader';

export function Account() {
  const { t } = useTranslation();
  const user = getUser()!;
  return (
    <>
      <PageHeader title={t('account')} />
      <Card>
        <p className="text-xl font-semibold">{user.name}</p>
        <p className="mt-1 text-muted">{t('flatLabel', { flat: user.flatNo })}</p>
        <div className="mt-6">
          <LanguageSwitcher />
        </div>
        <Button
          variant="danger"
          className="mt-6"
          onClick={() => {
            clearSession();
            window.location.assign('/login');
          }}
        >
          {t('logout')}
        </Button>
      </Card>
    </>
  );
}
