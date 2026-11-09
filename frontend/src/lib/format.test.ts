import { describe, expect, it } from 'vitest';
import {
  formatMoney,
  isOtp,
  isPhone,
  parseAmountPaise,
  parseRupeesToPaise,
  sanitizeRupeeInput,
  formatDuration,
  validPayAmount,
} from './format';
import { isResidentAccess } from './guard';
import en from '../locales/en.json';
import hi from '../locales/hi.json';
import mr from '../locales/mr.json';
describe('formatMoney', () => {
  it('formats paise with Indian grouping', () => expect(formatMoney(644749)).toBe('₹6,447.49'));
  it('handles zero and negative values', () => {
    expect(formatMoney(0)).toBe('₹0.00');
    expect(formatMoney(-1250)).toBe('-₹12.50');
  });
  describe('formatDuration', () => {
    it('formats minutes as compact days, hours, and minutes', () => {
      expect(formatDuration(45)).toBe('45 min');
      expect(formatDuration(90)).toBe('1 h 30 min');
      expect(formatDuration(1402.5)).toBe('23 h 22 min');
      expect(formatDuration(1740)).toBe('1 d 5 h');
    });
  });
});
describe('validation', () => {
  it('validates phone and otp', () => {
    expect(isPhone('9100000001')).toBe(true);
    expect(isPhone('5100000001')).toBe(false);
    expect(isOtp('123456')).toBe(true);
    expect(isOtp('12345')).toBe(false);
  });
  it('validates pay amount', () => {
    expect(validPayAmount(100, 100)).toBe(true);
    expect(validPayAmount(0, 100)).toBe(false);
    expect(validPayAmount(101, 100)).toBe(false);
  });
});
it('parses decimal rupees using half-up cents without float arithmetic', () => {
  expect(parseAmountPaise('6447.49')).toBe(644749);
  expect(parseAmountPaise('1.235')).toBe(124);
  expect(parseAmountPaise('nope')).toBeNull();
});
it('parses rupees safely from strings', () => {
  expect(parseRupeesToPaise('1000')).toBe(100000);
  expect(parseRupeesToPaise('1000.5')).toBe(100050);
  expect(parseRupeesToPaise('10.555')).toBe(1055);
  expect(parseRupeesToPaise('')).toBeNull();
  expect(parseRupeesToPaise('.')).toBeNull();
  expect(parseRupeesToPaise('0')).toBeNull();
  expect(parseRupeesToPaise('0.00')).toBeNull();
  expect(sanitizeRupeeInput('abc')).toBe('');
  expect(sanitizeRupeeInput('1e3')).toBe('13');
});
it('allows only resident sessions through the route guard', () => {
  expect(isResidentAccess('token', { name: 'Demo', role: 'RESIDENT', flatNo: 'A-101' })).toBe(true);
  expect(isResidentAccess('token', { name: 'Demo', role: 'ACCOUNTS', flatNo: null })).toBe(false);
  expect(isResidentAccess(null, null)).toBe(false);
});
function leaves(value: unknown, prefix = ''): Record<string, string> {
  if (typeof value === 'string') return { [prefix]: value };
  return Object.entries(value as Record<string, unknown>).reduce(
    (all, [key, child]) => ({ ...all, ...leaves(child, prefix ? `${prefix}.${key}` : key) }),
    {},
  );
}
it('keeps Hindi and Marathi resources complete and translated', () => {
  const english = leaves(en);
  const hindi = leaves(hi);
  const marathi = leaves(mr);
  expect(Object.keys(hindi)).toEqual(expect.arrayContaining(Object.keys(english)));
  expect(Object.keys(marathi)).toEqual(expect.arrayContaining(Object.keys(english)));
  const allowlist = new Set(['OTP']);
  const identical = [...Object.entries(english)]
    .filter(([key, value]) => value === hindi[key] || value === marathi[key])
    .filter(([, value]) => !allowlist.has(value));
  expect(identical.length).toBeLessThanOrEqual(1);
});
