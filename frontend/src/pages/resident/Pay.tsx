import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { ApiError, api } from '../../lib/api';
import {
  formatDate,
  formatMoney,
  paiseToInput,
  parseAmountPaise,
  validPayAmount,
} from '../../lib/format';
import type { Bill } from '../../types';
import { useLoad } from '../../hooks/useLoad';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { ErrorState } from '../../components/ErrorState';
import { FormField } from '../../components/FormField';
import { MoneyText } from '../../components/MoneyText';
import { PageHeader } from '../../components/PageHeader';
import { Skeleton } from '../../components/Skeleton';

export function Pay() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { data: bills, error, loading } = useLoad<Bill[]>(api.bills);
  const [amount, setAmount] = useState('');
  const [confirm, setConfirm] = useState(false);
  const [message, setMessage] = useState('');
  if (loading) return <Skeleton />;
  const bill = bills?.find((item) => item.id === Number(id));
  if (error || !bill)
    return (
      <ErrorState error={error || t('billNotFound')} retry={() => window.location.reload()} t={t} />
    );
  const paise = parseAmountPaise(amount) || 0;
  const submit = async () => {
    if (!validPayAmount(paise, bill.totalDuePaise)) return setMessage(t('amountInvalid'));
    try {
      const receipt = await api.pay(bill.id, paise);
      navigate(`/receipts/${receipt.paymentId}`, { state: receipt });
    } catch (reason) {
      setMessage(reason instanceof ApiError ? reason.message : t('paymentFailed'));
    }
  };
  return (
    <>
      <PageHeader title={t('payBill')} back={`/bills/${bill.id}`} />
      <Card>
        <p className="mb-1 text-muted">{formatDate(bill.billMonth, i18n.language)}</p>
        <p className="mb-6 text-2xl font-bold">
          <MoneyText paise={bill.totalDuePaise} />
        </p>
        <div className="mb-4 flex gap-2">
          <Button variant="secondary" onClick={() => setAmount(paiseToInput(bill.totalDuePaise))}>
            {t('fullAmount')}
          </Button>
          <Button variant="secondary" onClick={() => setAmount('')}>
            {t('otherAmount')}
          </Button>
        </div>
        <FormField label={t('amount')}>
          <input
            inputMode="decimal"
            aria-label={t('amount')}
            className="focus-ring w-full rounded-lg border border-slate-300 px-4 py-3 nums"
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
          />
        </FormField>
        {message && (
          <p aria-live="polite" className="mt-4 text-danger">
            {message}
          </p>
        )}
        {confirm ? (
          <div className="mt-6 rounded-lg bg-slate-50 p-4">
            <p className="font-semibold">{t('testMode')}</p>
            <p className="mt-2 text-muted">{t('confirmAmount', { amount: formatMoney(paise) })}</p>
            <Button className="mt-4" onClick={submit}>
              {t('confirmPayment')}
            </Button>
          </div>
        ) : (
          <Button
            className="mt-6"
            onClick={() =>
              validPayAmount(paise, bill.totalDuePaise)
                ? setConfirm(true)
                : setMessage(t('amountInvalid'))
            }
          >
            {t('payBill')}
          </Button>
        )}
      </Card>
    </>
  );
}
