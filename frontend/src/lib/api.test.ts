// @vitest-environment jsdom
import { act, createElement } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { WakingBanner } from '../components/WakingBanner';
import { api, getWakeState } from './api';

(globalThis as typeof globalThis & { IS_REACT_ACT_ENVIRONMENT: boolean }).IS_REACT_ACT_ENVIRONMENT =
  true;
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, values?: { current?: number; total?: number }) =>
      key === 'serverWaking'
        ? 'The server is waking up (free hosting). This can take up to 2 minutes. Please wait.'
        : `Attempt ${values?.current} of ${values?.total}`,
  }),
}));

function response(data: unknown) {
  return { ok: true, status: 200, json: async () => data };
}

describe('API wake handling', () => {
  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('retries a timed-out GET and succeeds', async () => {
    vi.useFakeTimers();
    let calls = 0;
    vi.stubGlobal(
      'fetch',
      vi.fn((_url: string, init: RequestInit) => {
        calls += 1;
        if (calls === 1) {
          return new Promise((_resolve, reject) => {
            init.signal?.addEventListener('abort', () =>
              reject(new DOMException('Timed out', 'AbortError')),
            );
          });
        }
        return Promise.resolve(response({}));
      }),
    );

    const result = api.me();
    await vi.advanceTimersByTimeAsync(25_000);
    expect(getWakeState().waking).toBe(true);
    await vi.advanceTimersByTimeAsync(10_000);

    await expect(result).resolves.toEqual({});
    expect(calls).toBe(2);
    expect(getWakeState().waking).toBe(false);
  });

  it('does not retry the pay POST', async () => {
    const fetchMock = vi.fn().mockRejectedValue(new TypeError('Network unavailable'));
    vi.stubGlobal('fetch', fetchMock);

    await expect(api.pay(4, 1000)).rejects.toThrow('Network unavailable');
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(getWakeState().waking).toBe(false);
  });

  it('shows the waking banner and hides it after success', async () => {
    vi.useFakeTimers();
    let calls = 0;
    vi.stubGlobal(
      'fetch',
      vi.fn((_url: string, init: RequestInit) => {
        calls += 1;
        if (calls === 1) {
          return new Promise((_resolve, reject) => {
            init.signal?.addEventListener('abort', () =>
              reject(new DOMException('Timed out', 'AbortError')),
            );
          });
        }
        return Promise.resolve(response({}));
      }),
    );
    const container = document.createElement('div');
    const root: Root = createRoot(container);
    document.body.appendChild(container);
    act(() => root.render(createElement(WakingBanner)));

    const request = api.me();
    await act(() => vi.advanceTimersByTimeAsync(25_000));
    expect(container.textContent).toContain('The server is waking up');
    await act(() => vi.advanceTimersByTimeAsync(10_000));
    await act(async () => request);
    expect(container.textContent).toBe('');

    act(() => root.unmount());
    container.remove();
  });
});
