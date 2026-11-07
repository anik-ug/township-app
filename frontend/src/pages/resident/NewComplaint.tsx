import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { api } from '../../lib/api';
import type { Complaint } from '../../types';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { FormField } from '../../components/FormField';
import { PageHeader } from '../../components/PageHeader';

const categories = [
  'PLUMBING',
  'ELECTRICAL',
  'LIFT',
  'SECURITY',
  'HOUSEKEEPING',
  'PARKING',
  'OTHER',
];

export function NewComplaint() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('');
  const [result, setResult] = useState<Complaint | null>(null);
  const [error, setError] = useState('');
  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    const trimmed = description.trim();
    if (trimmed.length < 10 || trimmed.length > 1000) return setError(t('descriptionLength'));
    try {
      setResult(await api.createComplaint(trimmed, category));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : t('requestFailed'));
    }
  };
  if (result)
    return (
      <>
        <PageHeader title={t('newComplaint')} back="/complaints" />
        <Card>
          <p className="font-semibold">
            {t(`categories.${result.category}`, { defaultValue: result.category })} ·{' '}
            {t(`urgencyLabels.${result.urgency}`, { defaultValue: t('unknownStatus') })}
          </p>
          <p className="mt-3">{t('assigned')}</p>
          {result.escalated && (
            <p className="mt-4 rounded-lg bg-amber-50 p-3 text-warning">{t('escalated')}</p>
          )}
          <Link className="mt-6 block" to={`/complaints/${result.id}`}>
            <Button>{t('viewComplaint')}</Button>
          </Link>
        </Card>
      </>
    );
  return (
    <>
      <PageHeader title={t('newComplaint')} back="/complaints" />
      <form className="space-y-5" onSubmit={submit}>
        <FormField label={t('description')}>
          <textarea
            aria-label={t('description')}
            className="focus-ring min-h-36 w-full rounded-lg border border-slate-300 px-4 py-3"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            maxLength={1000}
          />
          <span className="block text-right text-sm text-muted">
            {description.trim().length}/1000 {t('characters')}
          </span>
        </FormField>
        <FormField label={`${t('category')} (${t('optional')})`}>
          <select
            aria-label={t('category')}
            className="focus-ring w-full rounded-lg border border-slate-300 bg-white px-4 py-3"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
          >
            <option value="">{t('selectCategory')}</option>
            {categories.map((key) => (
              <option key={key} value={key}>
                {t(`categories.${key}`)}
              </option>
            ))}
          </select>
        </FormField>
        {error && (
          <p aria-live="polite" className="text-danger">
            {error}
          </p>
        )}
        <Button type="submit" disabled={description.trim().length < 10}>
          {t('submit')}
        </Button>
      </form>
    </>
  );
}
