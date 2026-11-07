import type { User } from '../types';
import { clearSession, getToken, TOKEN_KEY } from './api';

export { clearSession, getToken, TOKEN_KEY };

export const getUser = (fallback: User | null = null): User | null => {
  const value = localStorage.getItem('township_user');
  return value ? (JSON.parse(value) as User) : fallback;
};

export const saveSession = (token: string, user: User) => {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem('township_user', JSON.stringify(user));
};
