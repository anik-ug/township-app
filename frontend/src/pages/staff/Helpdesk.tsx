import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { api, ApiError } from '../../lib/api';
import { formatDateTime } from '../../lib/format';
import type { HelpdeskComplaint } from '../../types';
import { StaffLayout } from '../../layouts/StaffLayout';
import { SmallButton, StaffState } from './StaffStates';
import { validNextStatuses } from '../../lib/guard';
import { age } from './shared';

const categories = [
  'PLUMBING',
  'ELECTRICAL',
  'LIFT',
  'SECURITY',
  'HOUSEKEEPING',
  'PARKING',
  'OTHER',
];
export function Helpdesk() {
  const { t } = useTranslation();
  const [rows, setRows] = useState<HelpdeskComplaint[]>([]);
  const [selected, setSelected] = useState<HelpdeskComplaint>();
  const [filters, setFilters] = useState({
    status: '',
    category: '',
    urgency: '',
    tower: '',
    escalated: false,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [toast, setToast] = useState('');
  const load = () => {
    setLoading(true);
    setError('');
    const q = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => {
      if (v) q.set(k, String(v));
    });
    api
      .helpdeskComplaints(`?${q}`)
      .then(setRows)
      .catch((e) => setError(e instanceof Error ? e.message : t('requestFailed')))
      .finally(() => setLoading(false));
  };
  useEffect(load, [
    filters.status,
    filters.category,
    filters.urgency,
    filters.tower,
    filters.escalated,
  ]);
  const save = async (action: () => Promise<HelpdeskComplaint>) => {
    try {
      const result = await action();
      setSelected(result);
      setRows(rows.map((r) => (r.id === result.id ? result : r)));
      setToast(t('saved'));
    } catch (e) {
      setToast(e instanceof Error ? e.message : t('requestFailed'));
    }
  };
  return (
    <StaffLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">{t('helpdeskTitle')}</h1>
          <p className="text-muted">{t('complaintQueue')}</p>
        </div>
        <div className="flex flex-wrap gap-2 rounded-lg border bg-white p-3">
          {(['status', 'category', 'urgency', 'tower'] as const).map((key) => (
            <select
              key={key}
              aria-label={t(key)}
              className="focus-ring rounded border px-3 py-2"
              value={filters[key]}
              onChange={(e) => setFilters({ ...filters, [key]: e.target.value })}
            >
              <option value="">{t(`all${key[0].toUpperCase()}${key.slice(1)}`)}</option>
              {(key === 'category'
                ? categories
                : key === 'status'
                  ? ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
                  : key === 'urgency'
                    ? ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
                    : []
              ).map((x) => (
                <option key={x} value={x}>
                  {key === 'category'
                    ? t(`categories.${x}`)
                    : key === 'status'
                      ? t(`statusLabels.${x}`)
                      : key === 'urgency'
                        ? t(`urgencyLabels.${x}`)
                        : x}
                </option>
              ))}
            </select>
          ))}
          <label className="flex items-center gap-2 px-2">
            <input
              type="checkbox"
              checked={filters.escalated}
              onChange={(e) => setFilters({ ...filters, escalated: e.target.checked })}
            />
            {t('escalatedOnly')}
          </label>
        </div>
        {toast && (
          <p aria-live="polite" className="text-accent">
            {toast}
          </p>
        )}
        <StaffState loading={loading} error={error} empty={!rows.length} retry={load} />
        {!loading && !error && rows.length > 0 && (
          <div className="overflow-x-auto rounded-lg border bg-white">
            <table className="w-full text-left text-sm">
              <thead className="sticky top-0 bg-slate-50">
                <tr>
                  {['id', 'description', 'category', 'urgency', 'status', 'assignee', 'age'].map(
                    (h) => (
                      <th key={h} className="px-4 py-3">
                        {t(h)}
                      </th>
                    ),
                  )}
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.id}
                    onClick={() => setSelected(row)}
                    className={`cursor-pointer border-t hover:bg-slate-50 ${row.escalated ? 'border-l-4 border-l-amber-500' : ''}`}
                  >
                    <td className="px-4 py-3 nums">#{row.id}</td>
                    <td className="max-w-xs truncate px-4 py-3">{row.description}</td>
                    <td className="px-4 py-3">
                      <span>{t(`categories.${row.category}`)}</span>
                      <span className="mt-1 block w-fit rounded bg-slate-100 px-2 py-0.5 text-xs text-muted">
                        {t(`sourceLabels.${row.categorySource}`)}
                      </span>
                    </td>
                    <td
                      className={`px-4 py-3 ${row.urgency === 'CRITICAL' ? 'font-bold text-danger' : ''}`}
                    >
                      {t(`urgencyLabels.${row.urgency}`)}
                    </td>
                    <td className="px-4 py-3">
                      {t(`statusLabels.${row.status}`)}
                      {row.duplicateOf && (
                        <span className="ml-1 rounded-full bg-slate-100 px-2 text-xs">
                          {t('linkedTo', { id: row.duplicateOf })}
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3">{row.assigneeId ?? '—'}</td>
                    <td className="px-4 py-3 nums">{age(row.createdAt)}d</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {selected && (
          <div
            className="fixed inset-0 z-20 bg-black/30 p-4"
            onClick={() => setSelected(undefined)}
          >
            <section
              className="ml-auto h-full max-w-xl overflow-y-auto rounded-lg bg-white p-6"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex justify-between">
                <h2 className="text-xl font-bold">#{selected.id}</h2>
                <button className="tap" onClick={() => setSelected(undefined)}>
                  ×
                </button>
              </div>
              <p className="my-4">{selected.description}</p>
              {selected.escalated && (
                <p className="mb-4 rounded bg-amber-50 p-3 text-warning">
                  {t('escalatedReason', { reason: selected.escalationReason })}
                </p>
              )}
              <label className="block text-sm font-semibold">
                {t('category')}
                <select
                  className="focus-ring mt-1 w-full rounded border p-2"
                  value={selected.category}
                  onChange={(e) =>
                    void save(() => api.changeComplaintCategory(selected.id, e.target.value))
                  }
                >
                  {categories.map((c) => (
                    <option key={c} value={c}>
                      {t(`categories.${c}`)}
                    </option>
                  ))}
                </select>
              </label>
              <div className="my-5 flex flex-wrap gap-2">
                {validNextStatuses(selected.status).map((status) => (
                  <SmallButton
                    key={status}
                    onClick={() => void save(() => api.changeComplaintStatus(selected.id, status))}
                  >
                    {t(`statusLabels.${status}`)}
                  </SmallButton>
                ))}
              </div>
              <h3 className="font-semibold">{t('timeline')}</h3>
              <div className="mt-2 space-y-3">
                {selected.timeline.map((event, i) => (
                  <div key={i} className="border-l-2 border-accent pl-3">
                    <p>{event.note}</p>
                    <p className="text-xs text-muted">{formatDateTime(event.at)}</p>
                  </div>
                ))}
              </div>
            </section>
          </div>
        )}
      </div>
    </StaffLayout>
  );
}
