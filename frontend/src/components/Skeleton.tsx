import i18n from '../i18n';

export function Skeleton() {
  return (
    <div className="space-y-3" aria-label={i18n.t('loading')}>
      <div className="h-5 w-1/3 rounded bg-slate-200 motion-safe:animate-pulse" />
      <div className="h-10 w-2/3 rounded bg-slate-200 motion-safe:animate-pulse" />
    </div>
  );
}
