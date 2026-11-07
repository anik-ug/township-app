import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export function PageHeader({ title, back }: { title: string; back?: string }) {
  const { t } = useTranslation();
  return (
    <header className="mb-6 flex items-center gap-3">
      {back && (
        <Link
          className="tap focus-ring flex items-center text-2xl"
          to={back}
          aria-label={t('backLabel')}
        >
          ‹
        </Link>
      )}
      <h1 className="text-2xl font-bold">{title}</h1>
    </header>
  );
}
