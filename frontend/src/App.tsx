import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { isResidentAccess } from './lib/guard';
import { getToken } from './lib/session';
import { getUser } from './lib/session';
import { ResidentLayout } from './layouts/ResidentLayout';
import { Account } from './pages/resident/Account';
import { BillDetail } from './pages/resident/BillDetail';
import { Bills } from './pages/resident/Bills';
import { ComplaintDetail } from './pages/resident/ComplaintDetail';
import { Complaints } from './pages/resident/Complaints';
import { Home } from './pages/resident/Home';
import { Login } from './pages/resident/Login';
import { NewComplaint } from './pages/resident/NewComplaint';
import { Pay } from './pages/resident/Pay';
import { Receipt } from './pages/resident/Receipt';
import { WrongRole } from './pages/resident/WrongRole';

function Protected() {
  const token = getToken();
  return isResidentAccess(token, getUser()) ? (
    <Outlet />
  ) : (
    <Navigate to={token ? '/wrong-role' : '/login'} replace />
  );
}

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/wrong-role" element={<WrongRole />} />
        <Route element={<Protected />}>
          <Route element={<ResidentLayout />}>
            <Route index element={<Home />} />
            <Route path="/bills" element={<Bills />} />
            <Route path="/bills/:id" element={<BillDetail />} />
            <Route path="/bills/:id/pay" element={<Pay />} />
            <Route path="/receipts/:id" element={<Receipt />} />
            <Route path="/complaints" element={<Complaints />} />
            <Route path="/complaints/new" element={<NewComplaint />} />
            <Route path="/complaints/:id" element={<ComplaintDetail />} />
            <Route path="/account" element={<Account />} />
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
