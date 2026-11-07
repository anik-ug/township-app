import type { Bill, Complaint, Home, LedgerEntry, Receipt, User } from '../types';
const base = import.meta.env.VITE_API_URL || 'http://localhost:8090';
export const TOKEN_KEY = 'township_token';
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const clearSession = () => { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem('township_user'); };
export class ApiError extends Error { constructor(public status: number, public code: string, message: string) { super(message); } }
async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set('Content-Type', 'application/json');
  const token = getToken(); if (token) headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(`${base}${path}`, { ...init, headers });
  if (response.status === 401) { clearSession(); window.location.assign('/login'); throw new ApiError(401, 'UNAUTHORIZED', 'Authentication required'); }
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new ApiError(response.status, data.error || 'REQUEST_ERROR', data.message || 'Request failed');
  return data as T;
}
export const api = {
  requestOtp: (phone: string) => request<{ message: string }>('/api/auth/request-otp', { method: 'POST', body: JSON.stringify({ phone }) }),
  verifyOtp: (phone: string, otp: string) => request<{ token: string; role: string; name: string; flatNo: string | null }>('/api/auth/verify-otp', { method: 'POST', body: JSON.stringify({ phone, otp }) }),
  me: () => request<User>('/api/auth/me'),
  home: () => request<Home>('/api/resident/home'),
  bills: () => request<Bill[]>('/api/resident/bills'),
  ledger: () => request<LedgerEntry[]>('/api/resident/ledger'),
  pay: (id: number, amountPaise: number) => request<Receipt>(`/api/resident/bills/${id}/pay`, { method: 'POST', body: JSON.stringify({ amountPaise }) }),
  receipt: (id: number) => request<Receipt>(`/api/resident/receipts/${id}`),
  complaints: () => request<Complaint[]>('/api/resident/complaints'),
  complaint: (id: number) => request<Complaint>(`/api/resident/complaints/${id}`),
  createComplaint: (description: string, category?: string) => request<Complaint>('/api/resident/complaints', { method: 'POST', body: JSON.stringify({ description, category: category || null }) }),
  reopenComplaint: (id: number) => request<Complaint>(`/api/resident/complaints/${id}/reopen`, { method: 'POST' }),
  rateComplaint: (id: number, rating: number) => request<Complaint>(`/api/resident/complaints/${id}/rate`, { method: 'POST', body: JSON.stringify({ rating }) })
};
