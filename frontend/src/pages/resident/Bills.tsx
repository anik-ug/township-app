import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { api } from '../../lib/api';
import { formatDate, formatMoney } from '../../lib/format';
import type { Bill, LedgerEntry } from '../../types';
import { useLoad } from '../../hooks/useLoad';
import { Card } from '../../components/Card';
import { EmptyState } from '../../components/EmptyState';
import { ErrorState } from '../../components/ErrorState';
import { MoneyText } from '../../components/MoneyText';
import { PageHeader } from '../../components/PageHeader';
import { Skeleton } from '../../components/Skeleton';
import { StatusBadge } from '../../components/StatusBadge';

export function Bills() {
  const { t } = useTranslation();
  const { data, error, loading, retry } = useLoad<Bill[]>(api.bills);
  const [ledger, setLedger] = useState<LedgerEntry[] | null>(null);
  if (loading) return <Skeleton />;
  if (error || !data) return <ErrorState error={error || t('unableBills')} retry={retry} t={t} />;
  return (
    <>
      <PageHeader title={t('bills')} />
      {data.length === 0 ? (
        <EmptyState text={t('noData')} />
      ) : (
        <div className="space-y-3">
          {data.map((bill) => (
            <Link className="block" to={`/bills/${bill.id}`} key={bill.id}>
              <Card>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold">{formatDate(bill.billMonth, i18n.language)}</p>
                    <p className="mt-1 text-sm text-muted">
                      {t('dueOn')} {formatDate(bill.dueDate, i18n.language)}
                    </p>
                  </div>
                  <StatusBadge status={bill.status} />
                </div>
                <div className="mt-4 flex justify-between">
                  <span className="text-muted">{t('total')}</span>
                  <MoneyText paise={bill.totalDuePaise} />
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}
      <Card className="mt-6">
        <button
          className="focus-ring w-full text-left font-semibold"
          onClick={() => api.ledger().then(setLedger)}
        >
          ▾ {t('ledger')}
        </button>
        {ledger && (
          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-right text-sm">
              <thead>
                <tr className="border-b text-muted">
                  <th className="py-2 text-left">{t('date')}</th>
                  <th>{t('debit')}</th>
                  <th>{t('credit')}</th>
                  <th>{t('balance')}</th>
                </tr>
              </thead>
              <tbody>
                {ledger.map((entry, index) => (
                  <tr className="border-b border-slate-100" key={`${entry.entryDate}-${index}`}>
                    <td className="py-2 text-left">
                      {formatDate(entry.entryDate, i18n.language)}
                      <br />
                      <span className="text-xs text-muted">
                        {t(`ledgerLabels.${entry.type}`, { defaultValue: entry.type })}
                      </span>
                    </td>
                    <td className="nums">{formatMoney(entry.debitPaise)}</td>
                    <td className="nums">{formatMoney(entry.creditPaise)}</td>
                    <td className="nums">{formatMoney(entry.runningBalance)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}
