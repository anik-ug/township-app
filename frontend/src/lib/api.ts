import type {
  BankRow,
  Bill,
  Complaint,
  Dashboard,
  Home,
  HelpdeskComplaint,
  LedgerEntry,
  QueueView,
  Receipt,
  UploadView,
  User,
} from '../types';

export const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8090';
const REQUEST_TIMEOUT_MS = 25_000;
const RETRY_DELAY_MS = 10_000;
const MAX_RETRIES = 8;
const MAX_ATTEMPTS = MAX_RETRIES + 1;
type WakeState = { waking: boolean; attempt: number; maxAttempts: number };
const wakeListeners = new Set<() => void>();
let wakeState: WakeState = { waking: false, attempt: 0, maxAttempts: MAX_ATTEMPTS };
export const getWakeState = () => wakeState;
export const subscribeWakeState = (listener: () => void) => {
  wakeListeners.add(listener);
  return () => wakeListeners.delete(listener);
};
const setWakeState = (next: WakeState) => {
  wakeState = next;
  wakeListeners.forEach((listener) => listener());
};

export const TOKEN_KEY = 'township_token';
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const clearSession = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem('township_user');
};
export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
  ) {
    super(message);
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (!(init.body instanceof FormData)) headers.set('Content-Type', 'application/json');
  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const method = (init.method || 'GET').toUpperCase();
  const retryable =
    method === 'GET' ||
    (method === 'POST' && (path === '/api/auth/request-otp' || path === '/api/auth/verify-otp'));

  for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt += 1) {
    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
    try {
      const response = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers,
        signal: controller.signal,
      });
      if (response.status === 401) {
        clearSession();
        window.location.assign('/login');
        throw new ApiError(401, 'UNAUTHORIZED', 'Authentication required');
      }
      const data = await response.json().catch(() => ({}));
      if (!response.ok)
        throw new ApiError(
          response.status,
          data.error || 'REQUEST_ERROR',
          data.message || 'Request failed',
        );
      setWakeState({ waking: false, attempt: 0, maxAttempts: MAX_ATTEMPTS });
      return data as T;
    } catch (error) {
      const transient =
        error instanceof DOMException ? error.name === 'AbortError' : error instanceof TypeError;
      if (!retryable || !transient || attempt === MAX_ATTEMPTS) {
        setWakeState({ waking: false, attempt: 0, maxAttempts: MAX_ATTEMPTS });
        throw error;
      }
      setWakeState({ waking: true, attempt: attempt + 1, maxAttempts: MAX_ATTEMPTS });
      await new Promise<void>((resolve) => window.setTimeout(resolve, RETRY_DELAY_MS));
    } finally {
      window.clearTimeout(timeout);
    }
  }
  throw new Error('Request failed');
}

export const api = {
  requestOtp: (phone: string) =>
    request<{ message: string }>('/api/auth/request-otp', {
      method: 'POST',
      body: JSON.stringify({ phone }),
    }),
  verifyOtp: (phone: string, otp: string) =>
    request<{ token: string; role: string; name: string; flatNo: string | null }>(
      '/api/auth/verify-otp',
      { method: 'POST', body: JSON.stringify({ phone, otp }) },
    ),
  me: () => request<User>('/api/auth/me'),
  home: () => request<Home>('/api/resident/home'),
  bills: () => request<Bill[]>('/api/resident/bills'),
  ledger: () => request<LedgerEntry[]>('/api/resident/ledger'),
  pay: (id: number, amountPaise: number) =>
    request<Receipt>(`/api/resident/bills/${id}/pay`, {
      method: 'POST',
      body: JSON.stringify({ amountPaise }),
    }),
  receipt: (id: number) => request<Receipt>(`/api/resident/receipts/${id}`),
  complaints: () => request<Complaint[]>('/api/resident/complaints'),
  complaint: (id: number) => request<Complaint>(`/api/resident/complaints/${id}`),
  createComplaint: (description: string, category?: string) =>
    request<Complaint>('/api/resident/complaints', {
      method: 'POST',
      body: JSON.stringify({ description, category: category || null }),
    }),
  reopenComplaint: (id: number) =>
    request<Complaint>(`/api/resident/complaints/${id}/reopen`, { method: 'POST' }),
  rateComplaint: (id: number, rating: number) =>
    request<Complaint>(`/api/resident/complaints/${id}/rate`, {
      method: 'POST',
      body: JSON.stringify({ rating }),
    }),
  helpdeskComplaints: (params: string) =>
    request<HelpdeskComplaint[]>(`/api/helpdesk/complaints${params}`),
  changeComplaintCategory: (id: number, category: string) =>
    request<HelpdeskComplaint>(`/api/helpdesk/complaints/${id}/category`, {
      method: 'PATCH',
      body: JSON.stringify({ category }),
    }),
  changeComplaintStatus: (id: number, status: string) =>
    request<HelpdeskComplaint>(`/api/helpdesk/complaints/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),
  assignComplaint: (id: number, assigneeId: number) =>
    request<HelpdeskComplaint>(`/api/helpdesk/complaints/${id}/assign`, {
      method: 'PATCH',
      body: JSON.stringify({ assigneeId }),
    }),
  uploadBank: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return request<UploadView>('/api/accounts/bank-upload', { method: 'POST', body: form });
  },
  accountsQueue: () => request<QueueView[]>('/api/accounts/queue'),
  resolveQueue: (id: number, flatId: number, billId: number) =>
    request<BankRow>(`/api/accounts/queue/${id}/resolve`, {
      method: 'POST',
      body: JSON.stringify({ flatId, billId }),
    }),
  rejectQueue: (id: number, reason: string) =>
    request<BankRow>(`/api/accounts/queue/${id}/reject`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    }),
  committeeDashboard: () => request<Dashboard>('/api/committee/dashboard'),
};
