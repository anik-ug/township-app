export function formatMoney(paise: number): string {
  const sign = paise < 0 ? '-' : '';
  const absolute = Math.abs(Math.trunc(paise));
  const rupees = Math.floor(absolute / 100);
  const cents = String(absolute % 100).padStart(2, '0');
  return `${sign}₹${rupees.toLocaleString('en-IN')}.${cents}`;
}
export function parseAmountPaise(value: string): number | null {
  const match = value.trim().match(/^(?:0|[1-9]\d*)(?:\.(\d{0,}))?$/);
  if (!match) return null;
  const whole = value.split('.')[0];
  const fraction = (match[1] || '').padEnd(3, '0');
  const roundedCents = Number(fraction.slice(0, 2)) + (Number(fraction[2]) >= 5 ? 1 : 0);
  return Number(whole) * 100 + roundedCents;
}
export function sanitizeRupeeInput(value: string): string {
  const cleaned = value.replace(/[^0-9.]/g, '');
  const dot = cleaned.indexOf('.');
  const whole = (dot < 0 ? cleaned : cleaned.slice(0, dot)).slice(0, 9);
  if (dot < 0) return whole;
  const decimals = cleaned
    .slice(dot + 1)
    .replace(/\./g, '')
    .slice(0, 2);
  return `${whole}.${decimals}`;
}
export function parseRupeesToPaise(value: string): number | null {
  const sanitized = sanitizeRupeeInput(value);
  if (!sanitized || sanitized === '.' || !/^\d+(?:\.\d{0,2})?$/.test(sanitized)) return null;
  const [whole, fraction = ''] = sanitized.split('.');
  const paise = BigInt(whole) * 100n + BigInt(fraction.padEnd(2, '0') || '0');
  if (paise === 0n || paise > BigInt(Number.MAX_SAFE_INTEGER)) return null;
  return Number(paise);
}
export function formatDuration(minutes: number): string {
  const totalMinutes = Math.max(0, Math.floor(minutes));
  const days = Math.floor(totalMinutes / 1440);
  const hours = Math.floor((totalMinutes % 1440) / 60);
  const remainingMinutes = totalMinutes % 60;
  return (
    [
      days ? `${days} d` : '',
      hours ? `${hours} h` : '',
      remainingMinutes ? `${remainingMinutes} min` : '',
    ]
      .filter(Boolean)
      .join(' ') || '0 min'
  );
}
export const formatDate = (value: string | null, locale = 'en') =>
  value
    ? new Intl.DateTimeFormat(locale, { dateStyle: 'medium', timeZone: 'Asia/Kolkata' }).format(
        new Date(`${value}T00:00:00+05:30`),
      )
    : '';
export const formatDateTime = (value: string, locale = 'en') =>
  new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Asia/Kolkata',
  }).format(new Date(value));
export const isPhone = (value: string) => /^[6-9]\d{9}$/.test(value);
export const isOtp = (value: string) => /^\d{6}$/.test(value);
export const validPayAmount = (value: number, total: number) =>
  Number.isInteger(value) && value > 0 && value <= total;
export const paiseToInput = (paise: number) =>
  `${Math.floor(paise / 100)}.${String(paise % 100).padStart(2, '0')}`;
