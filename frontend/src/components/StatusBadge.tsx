import { useTranslation } from 'react-i18next';

const statusClass = (status: string, escalated: boolean) =>
  escalated
    ? 'bg-amber-50 text-warning'
    : status === 'OVERDUE' || status === 'CRITICAL'
      ? 'bg-red-50 text-danger'
      : status === 'PAID' || status === 'CLOSED'
        ? 'bg-emerald-50 text-success'
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
      <span
        className={`mr-1 h-1.5 w-1.5 rounded-full ${escalated ? 'bg-amber-500' : status === 'CRITICAL' || status === 'OVERDUE' ? 'bg-danger' : status === 'PAID' || status === 'CLOSED' ? 'bg-success' : 'bg-slate-400'}`}
      />
      {escalated ? `${t('urgent')} · ` : ''}
      {t(`statusLabels.${status}`, { defaultValue: t('unknownStatus') })}
    </span>
  );
}
