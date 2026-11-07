import { useTranslation } from 'react-i18next';
import { Button } from '../../components/Button';
import { Skeleton } from '../../components/Skeleton';
import { EmptyState } from '../../components/EmptyState';
import { ErrorState } from '../../components/ErrorState';
export function StaffState({
  loading,
  error,
  empty,
  retry,
}: {
  loading: boolean;
  error?: string;
  empty?: boolean;
  retry: () => void;
}) {
  const { t } = useTranslation();
  if (loading) return <Skeleton />;
  if (error) return <ErrorState error={error} retry={retry} t={t} />;
  return empty ? <EmptyState text={t('noData')} /> : null;
}
export function Stat({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <section className="card">
      <p className="text-sm text-muted">{label}</p>
      <p className="mt-2 text-2xl font-bold nums">{value}</p>
    </section>
  );
}
export function SmallButton({
  children,
  onClick,
  disabled = false,
}: {
  children: React.ReactNode;
  onClick: () => void;
  disabled?: boolean;
}) {
  return (
    <Button type="button" variant="secondary" disabled={disabled} onClick={onClick}>
      {children}
    </Button>
  );
}
