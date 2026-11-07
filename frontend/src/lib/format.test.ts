import { describe, expect, it } from 'vitest';
import { formatMoney, isOtp, isPhone, parseAmountPaise, validPayAmount } from './format';
import { isResidentAccess } from './guard';
describe('formatMoney', () => { it('formats paise with Indian grouping', () => expect(formatMoney(644749)).toBe('₹6,447.49')); it('handles zero and negative values', () => { expect(formatMoney(0)).toBe('₹0.00'); expect(formatMoney(-1250)).toBe('-₹12.50'); }); });
describe('validation', () => { it('validates phone and otp', () => { expect(isPhone('9100000001')).toBe(true); expect(isPhone('5100000001')).toBe(false); expect(isOtp('123456')).toBe(true); expect(isOtp('12345')).toBe(false); }); it('validates pay amount', () => { expect(validPayAmount(100, 100)).toBe(true); expect(validPayAmount(0, 100)).toBe(false); expect(validPayAmount(101, 100)).toBe(false); }); });
it('parses decimal rupees using half-up cents without float arithmetic', () => { expect(parseAmountPaise('6447.49')).toBe(644749); expect(parseAmountPaise('1.235')).toBe(124); expect(parseAmountPaise('nope')).toBeNull(); });
it('allows only resident sessions through the route guard', () => { expect(isResidentAccess('token', { name: 'Demo', role: 'RESIDENT', flatNo: 'A-101' })).toBe(true); expect(isResidentAccess('token', { name: 'Demo', role: 'ACCOUNTS', flatNo: null })).toBe(false); expect(isResidentAccess(null, null)).toBe(false); });
