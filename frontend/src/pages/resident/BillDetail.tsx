import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { api } from '../../lib/api';
import { formatDate } from '../../lib/format';
import type { Bill } from '../../types';
import { useLoad } from '../../hooks/useLoad';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { ErrorState } from '../../components/ErrorState';
import { MoneyText } from '../../components/MoneyText';
import { PageHeader } from '../../components/PageHeader';
import { Skeleton } from '../../components/Skeleton';
import { StatusBadge } from '../../components/StatusBadge';

export function BillDetail() {
  const { id } = useParams();
  const { t } = useTranslation();
  const { data: bills, error, loading, retry } = useLoad<Bill[]>(api.bills);
  if (loading) return <Skeleton />;
  const bill = bills?.find((item) => item.id === Number(id));
  if (error || !bill) return <ErrorState error={error || t('billNotFound')} retry={retry} t={t} />;
  return (
    <>
      <PageHeader title={t('billDetails')} back="/bills" />
      <Card>
        <div className="flex justify-between">
          <div>
            <p className="font-semibold">{formatDate(bill.billMonth, i18n.language)}</p>
            <StatusBadge status={bill.status} />
          </div>
          <MoneyText paise={bill.totalDuePaise} large />
        </div>
        <dl className="mt-6 space-y-3 border-t pt-4">
          <div className="flex justify-between">
            <dt>{t('principal')}</dt>
            <dd>
              <MoneyText paise={bill.amountPaise} />
            </dd>
          </div>
          <div className="flex justify-between">
            <dt>{t('paid')}</dt>
            <dd>
              <MoneyText paise={-bill.paidPaise} />
            </dd>
          </div>
          <div className="flex justify-between">
            <dt>{t('lateFee')}</dt>
            <dd>
              <MoneyText paise={bill.lateFeePaise} />
            </dd>
          </div>
          <div className="flex justify-between border-t pt-3 font-bold">
            <dt>{t('total')}</dt>
            <dd>
              <MoneyText paise={bill.totalDuePaise} />
            </dd>
          </div>
        </dl>
        {bill.totalDuePaise > 0 && (
          <Link className="mt-6 block" to={`/bills/${bill.id}/pay`}>
            <Button>{t('payBill')}</Button>
          </Link>
        )}
      </Card>
    </>
  );
}
