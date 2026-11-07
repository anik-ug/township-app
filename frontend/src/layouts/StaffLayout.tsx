import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { clearSession, getUser } from '../lib/session';
import { LanguageSwitcher } from '../components/LanguageSwitcher';

export function StaffLayout({ children }: { children?: React.ReactNode }) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const user = getUser();
  const logout = () => { clearSession(); navigate('/login'); };
  return (
    <div className="min-h-screen bg-surface">
      <header className="flex items-center justify-between border-b bg-white px-4 py-3 md:hidden">
        <button className="tap focus-ring text-2xl" aria-label={t('menu')} onClick={() => setOpen(!open)}>☰</button>
        <span className="font-bold text-accent">{t('appName')}</span>
        <span className="text-sm font-semibold">{t(`role.${user?.role}`, { defaultValue: user?.role })}</span>
      </header>
      <aside className={`fixed inset-y-0 z-10 w-64 border-r bg-white p-5 ${open ? 'block' : 'hidden'} md:block`}>
        <div className="mb-8 text-xl font-bold text-accent">{t('appName')}</div>
        <p className="mb-6 text-sm text-muted">{t(`role.${user?.role}`, { defaultValue: user?.role })}</p>
        <nav className="space-y-2">
          <NavLink to={user?.role === 'HELPDESK' ? '/helpdesk' : user?.role === 'ACCOUNTS' ? '/accounts' : '/committee'} className="block rounded-lg px-3 py-3 font-semibold hover:bg-slate-100">{t('dashboard')}</NavLink>
        </nav>
        <div className="absolute bottom-5 left-5 right-5 space-y-3">
          <LanguageSwitcher />
          <button className="tap focus-ring w-full rounded-lg border px-3 py-2 text-left" onClick={logout}>{t('logout')}</button>
        </div>
      </aside>
      <main className="md:ml-64"><div className="mx-auto max-w-7xl p-4 md:p-8">{children || <Outlet />}</div></main>
    </div>
  );
}
