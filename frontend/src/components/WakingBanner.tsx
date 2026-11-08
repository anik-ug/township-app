import { useSyncExternalStore } from 'react';
import { useTranslation } from 'react-i18next';
import { getWakeState, subscribeWakeState } from '../lib/api';

export function WakingBanner() {
  const { t } = useTranslation();
  const state = useSyncExternalStore(subscribeWakeState, getWakeState, getWakeState);
  if (!state.waking) return null;
  return (
    <div
      className="mb-4 flex items-center gap-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-3 text-sm text-amber-900"
      role="status"
      aria-live="polite"
    >
      <span className="h-4 w-4 shrink-0 rounded-full border-2 border-amber-700 border-t-transparent motion-safe:animate-spin" />
      <span>
        {t('serverWaking')}{' '}
        <span className="font-medium">
          {t('serverWakingAttempt', { current: state.attempt, total: state.maxAttempts })}
        </span>
      </span>
    </div>
  );
}
