import type { Dashboard, UploadView } from '../../types';
export const ageingData = (dashboard: Dashboard) =>
  ['0-30', '31-60', '61-90', '90+'].map(bucket => ({
    bucket,
    amount: dashboard.duesAgeing.buckets[bucket]?.totalDuePaise || 0,
  }));
export const uploadSummary = (upload: UploadView) =>
  ['matched', 'queued', 'duplicate', 'error'].map(key => ({ key, count: upload.counts[key] || 0 }));
