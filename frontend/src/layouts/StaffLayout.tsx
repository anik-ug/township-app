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
    <div className="min-h-screen bg-surface">
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
          className="fixed inset-0 z-10 bg-black/30 md:hidden"
          onClick={closeMenu}
        />
      )}
      <aside
        id={menuId}
        ref={menuRef}
        tabIndex={-1}
        role="dialog"
        aria-label={t('staffMenu')}
        className={`fixed inset-y-0 z-20 flex w-64 flex-col overflow-y-auto border-r bg-white p-5 ${open ? 'block' : 'hidden'} md:flex`}
      >
        <div className="mb-8 flex items-start justify-between text-xl font-bold text-accent">
          {t('appName')}{' '}
          <span className="text-sm font-semibold text-ink">
            · {t(`role.${user?.role}`, { defaultValue: user?.role })}
          </span>
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
            className="block rounded-lg px-3 py-3 font-semibold hover:bg-slate-100"
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
      <main className="md:ml-64">
        <div className="mx-auto max-w-7xl p-4 md:p-8">{children || <Outlet />}</div>
      </main>
    </div>
  );
}
