import { useEffect, useState } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { api } from '../../lib/api';
import { formatDate } from '../../lib/format';
import type { Receipt as ReceiptData } from '../../types';
import { Card } from '../../components/Card';
import { MoneyText } from '../../components/MoneyText';
import { PageHeader } from '../../components/PageHeader';
import { Skeleton } from '../../components/Skeleton';

export function Receipt() {
  const { id } = useParams();
  const location = useLocation();
  const { t } = useTranslation();
  const [receipt, setReceipt] = useState<ReceiptData | null>(
    (location.state as ReceiptData) || null,
  );
  useEffect(() => {
    if (!receipt) void api.receipt(Number(id)).then(setReceipt);
  }, [id, receipt]);
  if (!receipt) return <Skeleton />;
  return (
    <>
      <PageHeader title={t('receipt')} back="/bills" />
      <Card>
        <p className="text-sm text-muted">{receipt.receiptNo}</p>
        <h2 className="mt-2 text-3xl font-bold">
          <MoneyText paise={receipt.amountPaise} large />
        </h2>
        <dl className="mt-6 space-y-3">
          <div className="flex justify-between">
            <dt>{t('flat')}</dt>
            <dd>{receipt.flatNo}</dd>
          </div>
          <div className="flex justify-between">
            <dt>{t('month')}</dt>
            <dd>{formatDate(receipt.billMonth, i18n.language)}</dd>
          </div>
          <div className="flex justify-between">
            <dt>{t('principal')}</dt>
            <dd>
              <MoneyText paise={receipt.principalPaise} />
            </dd>
          </div>
          <div className="flex justify-between">
            <dt>{t('lateFee')}</dt>
            <dd>
              <MoneyText paise={receipt.lateFeePaise} />
            </dd>
          </div>
          <div className="flex justify-between border-t pt-3 font-semibold">
            <dt>{t('remaining')}</dt>
            <dd>
              <MoneyText paise={receipt.remainingDuePaise} />
            </dd>
          </div>
        </dl>
      </Card>
    </>
  );
}
