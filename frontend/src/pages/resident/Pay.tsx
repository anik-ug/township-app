import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { ApiError, api } from '../../lib/api';
import {
  formatDate,
  formatMoney,
  paiseToInput,
  parseRupeesToPaise,
  sanitizeRupeeInput,
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
  const [selected, setSelected] = useState<'full' | 'other'>('other');
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  if (loading) return <Skeleton />;
  const bill = bills?.find((item) => item.id === Number(id));
  if (error || !bill)
    return (
      <ErrorState error={error || t('billNotFound')} retry={() => window.location.reload()} t={t} />
    );
  const paise = parseRupeesToPaise(amount);
  const empty = amount === '';
  const overLimit = paise !== null && paise > bill.totalDuePaise;
  const invalid = empty || paise === null || paise <= 0 || overLimit;
  const validationMessage = empty
    ? t('amountRequired')
    : paise === null || paise <= 0
      ? t('amountPositive')
      : overLimit
        ? t('amountMaximum', { amount: formatMoney(bill.totalDuePaise) })
        : '';
  const submit = async (event?: React.FormEvent) => {
    event?.preventDefault();
    if (invalid || submitting || paise === null) {
      setMessage(validationMessage);
      return;
    }
    setMessage('');
    setSubmitting(true);
    try {
      const receipt = await api.pay(bill.id, paise);
      navigate(`/receipts/${receipt.paymentId}`, { state: receipt });
    } catch (reason) {
      setMessage(reason instanceof ApiError ? reason.message : t('paymentFailed'));
      setSubmitting(false);
    }
  };
  const chooseFull = () => {
    setSelected('full');
    setAmount(paiseToInput(bill.totalDuePaise));
    setMessage('');
  };
  const chooseOther = () => {
    setSelected('other');
    setAmount('');
    setMessage('');
  };
  return (
    <form onSubmit={submit} className="space-y-5">
      <PageHeader title={t('payBill')} back={`/bills/${bill.id}`} />
      <Card>
        <p className="text-sm text-muted">{formatDate(bill.billMonth, i18n.language)}</p>
        <p className="mt-2 text-3xl font-bold">
          <MoneyText paise={bill.totalDuePaise} />
        </p>
        <div
          className="mt-6 grid grid-cols-2 rounded-lg border border-slate-300 p-1"
          role="group"
          aria-label={t('paymentAmount')}
        >
          <button
            type="button"
            aria-pressed={selected === 'full'}
            className={`tap rounded-md px-3 py-2 font-semibold ${selected === 'full' ? 'bg-accent text-white' : 'text-ink'}`}
            onClick={chooseFull}
          >
            {t('fullAmount')}
          </button>
          <button
            type="button"
            aria-pressed={selected === 'other'}
            className={`tap rounded-md px-3 py-2 font-semibold ${selected === 'other' ? 'bg-accent text-white' : 'text-ink'}`}
            onClick={chooseOther}
          >
            {t('otherAmount')}
          </button>
        </div>
        <FormField label={t('amount')}>
          <div
            className={`flex h-12 items-center rounded-lg border ${message || validationMessage ? 'border-danger' : 'border-slate-300'} bg-white`}
          >
            <span className="px-4 text-muted">₹</span>
            <input
              inputMode="decimal"
              autoComplete="off"
              aria-label={t('amount')}
              aria-invalid={Boolean(message || validationMessage)}
              aria-describedby={message || validationMessage ? 'amount-error' : 'amount-help'}
              readOnly={selected === 'full'}
              placeholder="0.00"
              className="focus-ring h-full min-w-0 flex-1 rounded-r-lg px-2 nums outline-none"
              value={amount}
              onChange={(event) => setAmount(sanitizeRupeeInput(event.target.value))}
              onPaste={(event) => {
                event.preventDefault();
                setAmount(sanitizeRupeeInput(event.clipboardData.getData('text')));
              }}
            />
          </div>
          {(message || validationMessage) && (
            <span id="amount-error" role="alert" className="block text-sm text-danger">
              {message || validationMessage}
            </span>
          )}
          {!message && !validationMessage && (
            <span id="amount-help" className="text-sm text-muted">
              {t('amountHint')}
            </span>
          )}
        </FormField>
      </Card>
      <div className="sticky bottom-20 z-10 -mx-4 bg-surface px-4 pb-2 pt-2 md:static md:mx-0 md:px-0">
        <Button type="submit" disabled={invalid || submitting} className="h-12 w-full">
          {submitting ? (
            <span className="inline-flex items-center gap-2">
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
              {t('payNow')}
            </span>
          ) : (
            t('payBill')
          )}
        </Button>
        <p className="mt-3 text-center text-xs text-muted">{t('testModeNote')}</p>
      </div>
    </form>
  );
}
