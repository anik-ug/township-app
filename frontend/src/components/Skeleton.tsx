import i18n from '../i18n';
import { WakingBanner } from './WakingBanner';

export function Skeleton() {
  return (
    <div aria-label={i18n.t('loading')}>
      <WakingBanner />
      <div className="space-y-3">
        <div className="h-5 w-1/3 rounded bg-slate-200 motion-safe:animate-pulse" />
        <div className="h-10 w-2/3 rounded bg-slate-200 motion-safe:animate-pulse" />
      </div>
    </div>
  );
}
