import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export function BottomNav() {
  const { t } = useTranslation();
  const links = [
    ['/', '⌂', 'home'],
    ['/bills', '▤', 'bills'],
    ['/complaints', '□', 'complaints'],
    ['/account', '○', 'account'],
  ];
  return (
    <nav className="fixed inset-x-0 bottom-0 border-t border-slate-200 bg-white md:static md:mt-8 md:border-0">
      <div className="mx-auto flex max-w-md justify-around py-2 text-xs md:rounded-lg md:border md:border-slate-200">
        {links.map(([to, icon, key]) => (
          <Link
            className="focus-ring tap flex flex-col items-center justify-center gap-1 text-slate-600"
            key={to}
            to={to}
          >
            <span className="text-xl">{icon}</span>
            <span>{t(key)}</span>
          </Link>
        ))}
      </div>
    </nav>
  );
}
