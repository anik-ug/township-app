import { Outlet } from 'react-router-dom';
import { BottomNav } from '../components/BottomNav';

export function ResidentLayout() {
  return (
    <div className="min-h-screen pb-20 md:pb-0">
      <main className="mx-auto max-w-md px-4 py-6 md:max-w-2xl">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  );
}
