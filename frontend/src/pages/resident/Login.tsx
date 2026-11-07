import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { api } from '../../lib/api';
import { isOtp, isPhone } from '../../lib/format';
import { saveSession } from '../../lib/session';
import { rolePath } from '../../lib/guard';
import { Button } from '../../components/Button';
import { FormField } from '../../components/FormField';
import { LanguageSwitcher } from '../../components/LanguageSwitcher';

export function Login() {
  const { t } = useTranslation();
  const [step, setStep] = useState(1);
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      if (step === 1) {
        if (!isPhone(phone)) throw new Error(t('phoneInvalid'));
        await api.requestOtp(phone);
        setStep(2);
      } else {
        if (!isOtp(otp)) throw new Error(t('otpInvalid'));
        const result = await api.verifyOtp(phone, otp);
        saveSession(result.token, result);
        navigate(rolePath(result.role));
      }
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : t('requestFailed'));
    }
  };

  return (
    <div className="mx-auto flex min-h-screen max-w-md items-center px-4 py-10">
      <div className="mb-10 flex justify-end">
        <LanguageSwitcher />
      </div>
      <div className="w-full rounded-lg border border-slate-200 bg-white p-6">
        <div className="mb-10">
          <p className="mb-2 font-semibold text-accent">{t('appName')}</p>
          <h1 className="text-3xl font-bold">{t('loginTitle')}</h1>
        </div>
        <form className="space-y-6" onSubmit={submit}>
          {step === 1 ? (
            <FormField label={t('phone')}>
              <input
                autoFocus
                inputMode="numeric"
                maxLength={10}
                autoComplete="tel"
                className="focus-ring w-full rounded-lg border border-slate-300 px-4 py-3"
                value={phone}
                onChange={(event) => setPhone(event.target.value.replace(/\D/g, ''))}
              />
            </FormField>
          ) : (
            <FormField label={t('otp')}>
              <input
                autoFocus
                inputMode="numeric"
                maxLength={6}
                autoComplete="one-time-code"
                className="focus-ring w-full rounded-lg border border-slate-300 px-4 py-3 tracking-[.4em]"
                value={otp}
                onChange={(event) => setOtp(event.target.value.replace(/\D/g, ''))}
              />
            </FormField>
          )}
          {error && (
            <p aria-live="polite" className="text-danger">
              {error}
            </p>
          )}
          {step === 2 && (
            <button
              type="button"
              className="focus-ring text-sm text-accent"
              onClick={() => setStep(1)}
            >
              {t('back')}
            </button>
          )}
          <Button type="submit">{step === 1 ? t('sendOtp') : t('verify')}</Button>
          {step === 2 && import.meta.env.VITE_SHOW_DEMO_HINT === 'true' && (
            <p className="text-sm text-muted">{t('demoOtp')}</p>
          )}
        </form>
      </div>
    </div>
  );
}
