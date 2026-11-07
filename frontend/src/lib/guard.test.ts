import { describe, expect, it } from 'vitest';
import { rolePath, validNextStatuses } from './guard';
describe('staff role helpers', () => {
  it('redirects each role to its landing page', () => {
    expect(rolePath('RESIDENT')).toBe('/');
    expect(rolePath('HELPDESK')).toBe('/helpdesk');
    expect(rolePath('ACCOUNTS')).toBe('/accounts');
    expect(rolePath('COMMITTEE')).toBe('/committee');
  });
  it('returns only valid complaint transitions', () => {
    expect(validNextStatuses('OPEN')).toEqual(['ASSIGNED']);
    expect(validNextStatuses('RESOLVED')).toEqual(['IN_PROGRESS', 'CLOSED']);
    expect(validNextStatuses('CLOSED')).toEqual([]);
  });
});
