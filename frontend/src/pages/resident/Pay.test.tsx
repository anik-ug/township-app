// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';

vi.mock('react-i18next', () => ({ useTranslation: () => ({ t: (key: string) => key }) }));
vi.mock('../../i18n', () => ({ default: { language: 'en' } }));
vi.mock('../../hooks/useLoad', () => ({
  useLoad: () => ({
    data: [{ id: 9, billMonth: '2026-10-01', totalDuePaise: 100000 }],
    loading: false,
  }),
}));
vi.mock('../../lib/api', () => ({ api: { pay: vi.fn() }, ApiError: class extends Error {} }));
vi.mock('../../components/PageHeader', () => ({ PageHeader: () => <div /> }));

import { Pay } from './Pay';

(globalThis as typeof globalThis & { IS_REACT_ACT_ENVIRONMENT: boolean }).IS_REACT_ACT_ENVIRONMENT =
  true;

describe('Pay amount input', () => {
  afterEach(() => document.body.replaceChildren());

  it('sanitizes letters and controls payment button validity', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const root = createRoot(container);
    act(() =>
      root.render(
        <MemoryRouter initialEntries={['/bills/9/pay']}>
          <Routes>
            <Route path="/bills/:id/pay" element={<Pay />} />
          </Routes>
        </MemoryRouter>,
      ),
    );
    const input = container.querySelector('input') as HTMLInputElement;
    const button = container.querySelector('button[type="submit"]') as HTMLButtonElement;
    expect(button.disabled).toBe(true);
    const setValue = (value: string) => {
      const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set;
      setter?.call(input, value);
      input.dispatchEvent(new Event('input', { bubbles: true }));
    };
    act(() => {
      setValue('abc');
    });
    expect(input.value).toBe('');
    act(() => {
      setValue('1000');
    });
    expect(button.disabled).toBe(false);
    act(() => {
      setValue('1001');
    });
    expect(button.disabled).toBe(true);
    act(() => root.unmount());
  });
});
