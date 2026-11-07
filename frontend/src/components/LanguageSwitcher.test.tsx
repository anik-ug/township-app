// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { afterEach, describe, expect, it, vi } from 'vitest';

vi.mock('react-i18next', async (importOriginal) => ({
  ...(await importOriginal<typeof import('react-i18next')>()),
  useTranslation: () => ({ t: (key: string) => key }),
}));

import i18n from '../i18n';
import { LanguageSwitcher } from './LanguageSwitcher';

(globalThis as typeof globalThis & { IS_REACT_ACT_ENVIRONMENT: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

describe('LanguageSwitcher', () => {
  afterEach(() => {
    document.body.replaceChildren();
  });

  it.each(['en', 'hi', 'mr'])('shows fixed language names for %s UI', async (language) => {
    await act(async () => {
      await i18n.changeLanguage(language);
    });
    const container = document.createElement('div');
    document.body.appendChild(container);
    const root = createRoot(container);
    act(() => root.render(<LanguageSwitcher />));

    expect([...container.querySelectorAll('option')].map((option) => option.textContent)).toEqual([
      'English',
      'हिन्दी',
      'मराठी',
    ]);
    expect(container.querySelector('option[value="hi"]')?.getAttribute('lang')).toBe('hi');
    expect(container.querySelector('option[value="mr"]')?.getAttribute('lang')).toBe('mr');
    act(() => root.unmount());
  });
});
