import type { User } from '../types';
export const isResidentAccess = (token: string | null, user: User | null) =>
  Boolean(token && user?.role === 'RESIDENT');
export const rolePath = (role: string) =>
  ({ RESIDENT: '/', HELPDESK: '/helpdesk', ACCOUNTS: '/accounts', COMMITTEE: '/committee' })[
    role
  ] || '/wrong-role';
export const hasRoleAccess = (token: string | null, user: User | null, role: string) =>
  Boolean(token && user?.role === role);
export const validNextStatuses = (status: string) =>
  (
    ({
      OPEN: ['ASSIGNED'],
      ASSIGNED: ['IN_PROGRESS'],
      IN_PROGRESS: ['RESOLVED'],
      RESOLVED: ['IN_PROGRESS', 'CLOSED'],
      CLOSED: [],
    }) as Record<string, string[]>
  )[status] || [];
