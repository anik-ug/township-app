export type User = { name: string; role: string; flatNo: string | null };
export type T = (key: string, options?: Record<string, unknown>) => string;
export type Home = {
  flatNo: string | null;
  totalDuePaise: number;
  nextDueDate: string | null;
  oldestOverdueDays: number;
  openComplaintCount: number;
};
export type Bill = {
  id: number;
  flatNo: string;
  billMonth: string;
  amountPaise: number;
  paidPaise: number;
  lateFeePaise: number;
  totalDuePaise: number;
  status: string;
  daysOverdue: number;
  dueDate: string;
};
export type LedgerEntry = {
  entryDate: string;
  type: string;
  debitPaise: number;
  creditPaise: number;
  runningBalance: number;
};
export type Receipt = {
  paymentId: number;
  receiptNo: string;
  billMonth: string;
  flatNo: string;
  amountPaise: number;
  lateFeePaise: number;
  principalPaise: number;
  paidAt: string;
  remainingDuePaise: number;
};
export type ComplaintEvent = {
  fromStatus: string | null;
  toStatus: string;
  note: string;
  at: string;
};
export type Complaint = {
  id: number;
  description: string;
  category: string;
  urgency: string;
  categorySource: string;
  status: string;
  assigneeId: number | null;
  duplicateOf: number | null;
  escalated: boolean;
  escalationReason: string | null;
  reopenCount: number;
  rating: number | null;
  firstResponseAt: string | null;
  closedAt: string | null;
  createdAt: string;
  timeline: ComplaintEvent[];
};
