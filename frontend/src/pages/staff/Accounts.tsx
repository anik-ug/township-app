import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { api } from '../../lib/api';
import { formatMoney } from '../../lib/format';
import type { QueueView, UploadView } from '../../types';
import { StaffLayout } from '../../layouts/StaffLayout';
import { SmallButton, StaffState, Stat } from './StaffStates';
import { uploadSummary } from './mappings';
export { uploadSummary };
export function Accounts() {
  const { t } = useTranslation();
  const [upload, setUpload] = useState<UploadView>();
  const [queue, setQueue] = useState<QueueView[]>([]);
  const [resolving, setResolving] = useState<QueueView>();
  const [rejecting, setRejecting] = useState<QueueView>();
  const [flatId, setFlatId] = useState<number>();
  const [billId, setBillId] = useState('');
  const [rejectReason, setRejectReason] = useState('');
  const [fileName, setFileName] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [toast, setToast] = useState('');
  const load = () => {
    setLoading(true);
    api
      .accountsQueue()
      .then(setQueue)
      .catch((e) => setError(e instanceof Error ? e.message : t('requestFailed')))
      .finally(() => setLoading(false));
  };
  useEffect(load, []);
  const uploadFile = (file?: File) => {
    if (!file) return;
    setFileName(file.name);
    if (file.size > 1_048_576) return setToast(t('fileTooLarge'));
    setToast('');
    api
      .uploadBank(file)
      .then(setUpload)
      .catch((e) => setToast(e instanceof Error ? e.message : t('requestFailed')));
  };
  const resolve = async () => {
    if (!resolving || !flatId || !Number.isInteger(Number(billId)) || Number(billId) <= 0)
      return setToast(t('billIdPrompt'));
    try {
      await api.resolveQueue(resolving.id, flatId, Number(billId));
      setToast(t('saved'));
      setResolving(undefined);
      load();
    } catch (e) {
      setToast(e instanceof Error ? e.message : t('requestFailed'));
    }
  };
  const reject = async () => {
    const reason = rejectReason.trim();
    if (!rejecting || reason.length < 1 || reason.length > 500)
      return setToast(t('rejectReasonRequired'));
    try {
      await api.rejectQueue(rejecting.id, reason);
      setToast(t('saved'));
      setRejecting(undefined);
      setRejectReason('');
      load();
    } catch (e) {
      setToast(e instanceof Error ? e.message : t('requestFailed'));
    }
  };
  return (
    <StaffLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">{t('accountsTitle')}</h1>
          <p className="text-muted">{t('unclearPayments')}</p>
        </div>
        <button
          type="button"
          className="tap rounded-lg bg-accent px-4 py-3 font-semibold text-white hover:bg-accent-dark"
          onClick={() => fileInputRef.current?.click()}
        >
          {t('uploadCsv')}
        </button>
        <input
          ref={fileInputRef}
          className="hidden"
          type="file"
          accept=".csv,text/csv"
          onChange={(e) => uploadFile(e.target.files?.[0])}
        />
        {fileName && <p className="text-sm text-muted">{fileName}</p>}
        {toast && (
          <p aria-live="polite" className="text-danger">
            {toast}
          </p>
        )}
        {upload && (
          <>
            <div className="grid gap-3 sm:grid-cols-4">
              {uploadSummary(upload).map((item) => (
                <Stat key={item.key} label={t(item.key)} value={item.count} />
              ))}
            </div>
            <div className="overflow-x-auto rounded-lg border bg-white">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50">
                  <tr>
                    {['status', 'confidence', 'amount', 'reference', 'why'].map((h) => (
                      <th className="px-4 py-3" key={h}>
                        {t(h)}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {upload.rows.map((row) => (
                    <tr className="border-t" key={row.id}>
                      <td className="px-4 py-3">
                        {t(`bankStatusLabels.${row.status}`, { defaultValue: t('unknownStatus') })}
                      </td>
                      <td className="px-4 py-3">
                        <div className="h-2 w-24 rounded bg-slate-200">
                          <div
                            className="h-2 rounded bg-accent"
                            style={{ width: `${row.confidence}%` }}
                          />
                        </div>
                        <span className="text-xs">{row.confidence}%</span>
                      </td>
                      <td className="px-4 py-3 nums">
                        {row.status === 'ERROR' ? row.rawAmount : formatMoney(row.amountPaise)}
                      </td>
                      <td className="px-4 py-3">{row.reference}</td>
                      <td className="max-w-sm px-4 py-3">{row.explanation}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
        <section className="space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold">{t('accountsQueue')}</h2>
            <SmallButton onClick={load}>{t('retry')}</SmallButton>
          </div>
          <StaffState loading={loading} error={error} empty={!queue.length} retry={load} />
          {!loading &&
            !error &&
            queue.map((item) => (
              <article className="card" key={item.id}>
                <div className="flex flex-wrap justify-between gap-3">
                  <div>
                    <p className="font-semibold">
                      {item.row.reference} · {formatMoney(item.row.amountPaise)}
                    </p>
                    <p className="text-muted">{item.row.explanation}</p>
                  </div>
                  <div className="flex gap-2">
                    <SmallButton
                      onClick={() => {
                        setResolving(item);
                        setFlatId(item.candidates[0]?.flatId);
                      }}
                    >
                      {t('resolve')}
                    </SmallButton>
                    <SmallButton
                      onClick={() => {
                        setRejecting(item);
                        setRejectReason('');
                      }}
                    >
                      {t('reject')}
                    </SmallButton>
                  </div>
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  {item.candidates.map((c) => (
                    <span className="rounded bg-slate-100 px-2 py-1 text-sm" key={c.flatId}>
                      {c.flatNo} · {formatMoney(c.openDuePaise)} {t('openDue')}
                    </span>
                  ))}
                </div>
              </article>
            ))}
        </section>
        {resolving && (
          <div className="fixed inset-0 z-20 bg-black/30 p-4">
            <section
              role="dialog"
              aria-modal="true"
              className="mx-auto mt-20 max-w-md rounded-lg bg-white p-6"
            >
              <h2 className="text-xl font-bold">{t('resolve')}</h2>
              <label className="mt-4 block text-sm font-semibold">
                {t('flat')}
                <select
                  className="focus-ring mt-1 w-full rounded border p-2"
                  value={flatId || ''}
                  onChange={(e) => setFlatId(Number(e.target.value))}
                >
                  {resolving.candidates.map((c) => (
                    <option key={c.flatId} value={c.flatId}>
                      {c.flatNo} · {formatMoney(c.openDuePaise)}
                    </option>
                  ))}
                </select>
              </label>
              <label className="mt-4 block text-sm font-semibold">
                {t('bill')}
                <input
                  className="focus-ring mt-1 w-full rounded border p-2"
                  inputMode="numeric"
                  value={billId}
                  onChange={(e) => setBillId(e.target.value.replace(/\D/g, ''))}
                />
              </label>
              <p className="mt-2 text-sm text-muted">{t('billTotalValidation')}</p>
              <div className="mt-5 flex justify-end gap-2">
                <SmallButton onClick={() => setResolving(undefined)}>{t('back')}</SmallButton>
                <SmallButton onClick={() => void resolve()}>{t('resolve')}</SmallButton>
              </div>
            </section>
          </div>
        )}
        {rejecting && (
          <div
            className="fixed inset-0 z-20 bg-black/30 p-4"
            onClick={() => setRejecting(undefined)}
          >
            <section
              role="dialog"
              aria-modal="true"
              className="mx-auto mt-20 max-w-md rounded-lg bg-white p-6"
              onClick={(e) => e.stopPropagation()}
            >
              <h2 className="text-xl font-bold">{t('reject')}</h2>
              <textarea
                autoFocus
                maxLength={500}
                required
                className="focus-ring mt-4 min-h-28 w-full rounded border p-2"
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder={t('rejectReason')}
              />
              <p className="mt-1 text-xs text-muted">{rejectReason.length}/500</p>
              <div className="mt-5 flex justify-end gap-2">
                <SmallButton onClick={() => setRejecting(undefined)}>{t('back')}</SmallButton>
                <SmallButton onClick={() => void reject()} disabled={!rejectReason.trim()}>
                  {t('reject')}
                </SmallButton>
              </div>
            </section>
          </div>
        )}
      </div>
    </StaffLayout>
  );
}
