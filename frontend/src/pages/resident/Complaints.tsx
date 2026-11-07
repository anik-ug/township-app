import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { api } from '../../lib/api';
import { formatDateTime } from '../../lib/format';
import type { Complaint } from '../../types';
import { useLoad } from '../../hooks/useLoad';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { EmptyState } from '../../components/EmptyState';
import { ErrorState } from '../../components/ErrorState';
import { Skeleton } from '../../components/Skeleton';
import { StatusBadge } from '../../components/StatusBadge';

export function Complaints() {
  const { t } = useTranslation();
  const { data, error, loading, retry } = useLoad<Complaint[]>(api.complaints);
  if (loading) return <Skeleton />;
  if (error || !data)
    return <ErrorState error={error || t('unableComplaints')} retry={retry} t={t} />;
  return (
    <>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t('complaints')}</h1>
        <Link to="/complaints/new">
          <Button>{t('newComplaint')}</Button>
        </Link>
      </div>
      {data.length === 0 ? (
        <EmptyState text={t('noData')} />
      ) : (
        <div className="space-y-3">
          {data.map((complaint) => (
            <Link className="block" to={`/complaints/${complaint.id}`} key={complaint.id}>
              <Card>
                <div className="flex justify-between gap-3">
                  <p className="line-clamp-2 font-medium">{complaint.description}</p>
                  <StatusBadge status={complaint.status} escalated={complaint.escalated} />
                </div>
                <p className="mt-3 text-sm text-muted">
                  {t(`categories.${complaint.category}`, { defaultValue: complaint.category })} ·{' '}
                  {formatDateTime(complaint.createdAt, i18n.language)}
                </p>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
