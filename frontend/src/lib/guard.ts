import type { User } from '../types';
export const isResidentAccess = (token: string | null, user: User | null) => Boolean(token && user?.role === 'RESIDENT');
