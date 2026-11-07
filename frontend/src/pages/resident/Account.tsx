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
          className="mt-6 w-full"
          onClick={() => {
            clearSession();
            window.location.assign('/login');
          }}
        >
          <span className="inline-flex items-center gap-2">
            <svg
              className="h-4 w-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.8"
                d="M10 5H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h4m5-4 4-3-4-3m4 3H9"
              />
            </svg>
            {t('logout')}
          </span>
        </Button>
      </Card>
    </>
  );
}
