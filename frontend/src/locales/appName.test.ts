import { describe, expect, it } from 'vitest';
import en from './en.json';
import hi from './hi.json';
import mr from './mr.json';

describe('localized app name', () => {
  it('uses Township in every supported locale', () => {
    expect(en.appName).toBe('Township');
    expect(hi.appName).toBe('टाउनशिप');
    expect(mr.appName).toBe('टाउनशिप');
  });
});
