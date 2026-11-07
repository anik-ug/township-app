import { describe, expect, it } from 'vitest';
import { ageingData, uploadSummary } from './mappings';
import type { Dashboard, UploadView } from '../../types';
const dashboard: Dashboard = {
  collections: {
    billMonth: '',
    billedPaise: 0,
    collectedPaise: 0,
    collectionRatePct: 0,
    flatsBilled: 0,
    flatsFullyPaid: 0,
    onTimeRatePct: 0,
    paymentsLast30DaysPaise: 0,
    paidByDay10Pct: 0,
  },
  duesAgeing: {
    buckets: {
      '0-30': { flatCount: 0, billCount: 0, totalDuePaise: 100 },
      '31-60': { flatCount: 0, billCount: 0, totalDuePaise: 200 },
      '61-90': { flatCount: 0, billCount: 0, totalDuePaise: 0 },
      '90+': { flatCount: 0, billCount: 0, totalDuePaise: 50 },
    },
    totalOutstandingPaise: 0,
    topFlats: [],
  },
  complaints: {
    openCount: 0,
    escalatedOpenCount: 0,
    byCategory: {},
    avgFirstResponseMinutes: 0,
    avgResolutionHours: 0,
    reopenedPct: 0,
    slaBreaches: 0,
    responseExcludedCount: 0,
    resolutionExcludedCount: 0,
  },
  accountsQueue: { queuedCount: 0, queuedAmountPaise: 0 },
  asOfDate: '',
  generatedAt: '',
};
describe('staff dashboard mappings', () => {
  it('maps all ageing buckets in order', () => {
    expect(ageingData(dashboard).map((x) => x.bucket)).toEqual(['0-30', '31-60', '61-90', '90+']);
    expect(ageingData(dashboard)[1].amount).toBe(200);
  });
  it('renders upload summary counts including zero values', () => {
    const upload: UploadView = {
      id: 1,
      rows: [],
      counts: { matched: 2, queued: 1, duplicate: 3, error: 0 },
    };
    expect(uploadSummary(upload)).toEqual([
      { key: 'matched', count: 2 },
      { key: 'queued', count: 1 },
      { key: 'duplicate', count: 3 },
      { key: 'error', count: 0 },
    ]);
  });
});
