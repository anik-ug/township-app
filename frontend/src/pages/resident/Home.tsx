import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import i18n from '../../i18n';
import { api } from '../../lib/api';
import { formatDate } from '../../lib/format';
import { getUser } from '../../lib/session';
import type { Bill, Home as HomeData } from '../../types';
import { useLoad } from '../../hooks/useLoad';
import { Button } from '../../components/Button';
import { Card } from '../../components/Card';
import { ErrorState } from '../../components/ErrorState';
import { MoneyText } from '../../components/MoneyText';
import { Skeleton } from '../../components/Skeleton';

export function Home() {
  const { t } = useTranslation();
  const { data, error, loading, retry } = useLoad<HomeData>(api.home);
  const bills = useLoad<Bill[]>(api.bills);
  const user = getUser()!;
  if (loading) return <Skeleton />;
  if (error || !data) return <ErrorState error={error || t('unableHome')} retry={retry} t={t} />;
  const next = bills.data
    ?.filter((bill) => bill.totalDuePaise > 0)
    .sort((a, b) => a.dueDate.localeCompare(b.dueDate))[0];
  return (
    <>
      <div className="mb-6">
        <p className="text-muted">{user.flatNo || data.flatNo}</p>
        <h1 className="text-2xl font-bold">
          {t('welcome')}, {user.name}
        </h1>
      </div>
      <Card className="mb-4 border-accent/20">
        <p className="mb-2 text-muted">{t('totalDue')}</p>
        <div className="text-3xl font-bold">
          <MoneyText paise={data.totalDuePaise} large />
        </div>
        {data.totalDuePaise > 0 ? (
          <>
            <p className="mt-3 text-sm text-muted">
              {data.oldestOverdueDays > 0
                ? `${data.oldestOverdueDays} ${t('daysOverdue')}`
                : `${t('dueOn')} ${formatDate(data.nextDueDate, i18n.language)}`}
            </p>
            {next && (
              <Link className="mt-5 block" to={`/bills/${next.id}/pay`}>
                <Button className="w-full">{t('payNow')}</Button>
              </Link>
            )}
          </>
        ) : (
          <p className="mt-3 font-semibold text-accent">{t('allPaid')}</p>
        )}
      </Card>
      <Link className="block" to="/complaints">
        <Card>
          <p className="text-muted">{t('openComplaints')}</p>
          <p className="mt-2 text-3xl font-bold nums">{data.openComplaintCount}</p>
        </Card>
      </Link>
    </>
  );
}
