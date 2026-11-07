import { useTranslation } from 'react-i18next';

const statusClass = (status: string, escalated: boolean) =>
  escalated
    ? 'bg-amber-50 text-warning'
    : status === 'OVERDUE'
      ? 'bg-red-50 text-danger'
      : status === 'PAID' || status === 'CLOSED'
        ? 'bg-emerald-50 text-accent-dark'
        : 'bg-slate-100 text-slate-700';

export function StatusBadge({
  status,
  escalated = false,
}: {
  status: string;
  escalated?: boolean;
}) {
  const { t } = useTranslation();
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(status, escalated)}`}
    >
      {escalated ? `${t('urgent')} · ` : ''}
      {t(`statusLabels.${status}`, { defaultValue: status.replaceAll('_', ' ') })}
    </span>
  );
}
