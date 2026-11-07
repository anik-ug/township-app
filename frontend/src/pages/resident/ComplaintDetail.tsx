import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { api } from '../../lib/api';
import { formatDateTime } from '../../lib/format';
import type { Complaint } from '../../types';
import { useLoad } from '../../hooks/useLoad';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { ErrorState } from '../../components/ErrorState';
import { PageHeader } from '../../components/PageHeader';
import { Skeleton } from '../../components/Skeleton';
import { StatusBadge } from '../../components/StatusBadge';

export function ComplaintDetail() {
  const { id } = useParams();
  const { t } = useTranslation();
  const { data, error, loading, retry } = useLoad<Complaint>(() => api.complaint(Number(id)));
  const [rating, setRating] = useState(0);
  if (loading) return <Skeleton />;
  if (error || !data)
    return <ErrorState error={error || t('complaintNotFound')} retry={retry} t={t} />;
  return (
    <>
      <PageHeader title={t('complaints')} back="/complaints" />
      <Card>
        <div className="flex justify-between gap-3">
          <h2 className="font-semibold">{data.description}</h2>
          <StatusBadge status={data.status} escalated={data.escalated} />
        </div>
        <p className="mt-3 text-muted">
          {t(`categories.${data.category}`, { defaultValue: data.category })} ·{' '}
          {t(`urgencyLabels.${data.urgency}`, { defaultValue: data.urgency })}
        </p>
        {data.status === 'RESOLVED' && (
          <Button
            variant="secondary"
            className="mt-5"
            onClick={() => void api.reopenComplaint(data.id).then(retry)}
          >
            {t('reopen')}
          </Button>
        )}
        {data.status === 'CLOSED' && data.rating == null && (
          <div className="mt-6">
            <p className="mb-2 font-medium">{t('rate')}</p>
            {[1, 2, 3, 4, 5].map((number) => (
              <button
                type="button"
                className="tap focus-ring text-2xl text-warning"
                key={number}
                aria-label={`${number} ${t('rate')}`}
                onClick={() => {
                  setRating(number);
                  void api.rateComplaint(data.id, number).then(retry);
                }}
              >
                {number <= rating ? '★' : '☆'}
              </button>
            ))}
          </div>
        )}
        <h3 className="mt-8 border-t pt-5 font-semibold">{t('timeline')}</h3>
        <ol className="mt-4 space-y-5 border-l border-slate-200 pl-5">
          {data.timeline.map((event, index) => (
            <li
              className="relative before:absolute before:-left-[25px] before:top-1 before:h-2 before:w-2 before:rounded-full before:bg-accent"
              key={`${event.at}-${index}`}
            >
              <p className="font-medium">{event.note}</p>
              <p className="mt-1 text-sm text-muted">{formatDateTime(event.at, i18n.language)}</p>
            </li>
          ))}
        </ol>
      </Card>
    </>
  );
}
