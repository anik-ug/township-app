import type { Complaint } from '../../types';
export const statusLabel = (status: string) => status.replaceAll('_', ' ');
export const age = (createdAt: string) =>
  Math.max(0, Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000));
export const isDuplicateOpen = (complaint: Complaint) =>
  complaint.duplicateOf !== null && complaint.status === 'CLOSED';
