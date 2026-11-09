import { useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { clearSession, getUser } from '../lib/session';
import { LanguageSwitcher } from '../components/LanguageSwitcher';

export function StaffLayout({ children }: { children?: React.ReactNode }) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);
  const menuButtonRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLElement>(null);
  const menuId = 'staff-menu';
  const user = getUser();
  const logout = () => {
    clearSession();
    navigate('/login');
  };
  const closeMenu = () => {
    setOpen(false);
    menuButtonRef.current?.focus();
  };
  useEffect(() => {
    closeMenu();
  }, [location.pathname]);
  useEffect(() => {
    if (!open) {
      document.body.style.overflow = '';
      return;
    }
    document.body.style.overflow = 'hidden';
    menuRef.current?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') closeMenu();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = '';
    };
  }, [open]);
  return (
    <div className="flex min-h-dvh flex-col bg-surface md:flex-row">
      <header className="flex items-center justify-between border-b bg-white px-4 py-3 md:hidden">
        <button
          ref={menuButtonRef}
          className="tap focus-ring text-2xl"
          aria-label={t('menu')}
          aria-expanded={open}
          aria-controls={menuId}
          onClick={() => (open ? closeMenu() : setOpen(true))}
        >
          ☰
        </button>
        <span className="font-bold text-accent">
          {t('appName')}{' '}
          <span className="text-sm font-semibold text-ink">
            · {t(`role.${user?.role}`, { defaultValue: user?.role })}
          </span>
        </span>
      </header>
      {open && (
        <button
          aria-label={t('closeMenu')}
          className="fixed inset-0 z-30 bg-black/30 md:hidden"
          onClick={closeMenu}
        />
      )}
      <aside
        id={menuId}
        ref={menuRef}
        tabIndex={-1}
        role="dialog"
        aria-label={t('staffMenu')}
        className={`fixed inset-y-0 left-0 z-40 flex h-dvh w-60 max-w-[85vw] flex-col overflow-y-auto border-r bg-white p-5 transition-transform duration-150 ${open ? 'translate-x-0' : 'hidden -translate-x-full'} md:static md:sticky md:top-0 md:flex md:h-screen md:w-60 md:max-w-none md:shrink-0 md:translate-x-0`}
      >
        <div className="mb-8 flex items-start justify-between">
          <div>
            <p className="text-xl font-bold text-accent">{t('appName')}</p>
            <span className="mt-2 inline-flex rounded-full bg-slate-100 px-2 py-1 text-xs font-semibold text-muted">
              {t(`role.${user?.role}`, { defaultValue: user?.role })}
            </span>
          </div>
          <button
            className="tap focus-ring -mr-2 -mt-2 text-2xl md:hidden"
            aria-label={t('closeMenu')}
            onClick={closeMenu}
          >
            ×
          </button>
        </div>
        <nav className="space-y-2" aria-label={t('staffMenu')}>
          <NavLink
            onClick={closeMenu}
            to={
              user?.role === 'HELPDESK'
                ? '/helpdesk'
                : user?.role === 'ACCOUNTS'
                  ? '/accounts'
                  : '/committee'
            }
            className={({ isActive }) =>
              `flex min-h-11 items-center rounded-r-lg border-l-2 px-3 font-semibold transition-colors duration-150 ${
                isActive
                  ? 'border-accent bg-accent/10 text-accent'
                  : 'border-transparent hover:bg-slate-100'
              }`
            }
          >
            {t(
              user?.role === 'HELPDESK'
                ? 'staffNav.complaints'
                : user?.role === 'ACCOUNTS'
                  ? 'staffNav.bankStatements'
                  : 'staffNav.dashboard',
            )}
          </NavLink>
        </nav>
        <div className="mt-auto space-y-3 pt-8">
          <LanguageSwitcher />
          <button
            className="tap focus-ring w-full rounded-lg border border-danger px-3 py-2 text-left text-danger hover:bg-red-50"
            onClick={logout}
          >
            <span className="inline-flex items-center gap-2">
              <svg
                className="h-4 w-4"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="1.8"
                  d="M10 5H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h4m5-4 4-3-4-3m4 3H9"
                />
              </svg>
              {t('logout')}
            </span>
          </button>
        </div>
      </aside>
      <main className="min-w-0 flex-1">
        <div className="mx-auto w-full max-w-7xl px-4 py-6 md:px-8">{children || <Outlet />}</div>
      </main>
    </div>
  );
}
