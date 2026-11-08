import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export function BottomNav() {
  const { t } = useTranslation();
  const links = [
    ['/', 'home'],
    ['/bills', 'bills'],
    ['/complaints', 'complaints'],
    ['/account', 'account'],
  ];
  return (
    <nav className="fixed inset-x-0 bottom-0 border-t border-slate-200 bg-white">
      <div className="mx-auto flex max-w-md justify-around pb-[env(safe-area-inset-bottom)] pt-2 text-xs md:max-w-2xl md:rounded-lg md:border md:border-slate-200">
        {links.map(([to, key]) => (
          <NavLink
            className={({ isActive }) =>
              `focus-ring tap flex flex-col items-center justify-center gap-1 transition-colors duration-150 ${isActive ? 'font-semibold text-accent' : 'text-slate-600'}`
            }
            key={to}
            to={to}
          >
            <svg
              className="h-5 w-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              aria-hidden="true"
            >
              {key === 'home' && (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1Z"
                />
              )}
              {key === 'bills' && (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M6 3h12v18H6zM9 7h6M9 11h6M9 15h4"
                />
              )}
              {key === 'complaints' && <path strokeLinecap="round" d="M6 4h12v13H9l-3 3z" />}
              {key === 'account' && (
                <>
                  <circle cx="12" cy="8" r="3" />
                  <path strokeLinecap="round" d="M5 21a7 7 0 0 1 14 0" />
                </>
              )}
            </svg>
            <span>{t(key)}</span>
          </NavLink>
        ))}
      </div>
    </nav>
  );
}
