// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { MemoryRouter, Routes, Route, useNavigate } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { StaffLayout } from './StaffLayout';

(globalThis as typeof globalThis & { IS_REACT_ACT_ENVIRONMENT: boolean }).IS_REACT_ACT_ENVIRONMENT =
  true;
vi.mock('react-i18next', () => ({ useTranslation: () => ({ t: (key: string) => key }) }));
vi.mock('../components/LanguageSwitcher', () => ({ LanguageSwitcher: () => <div /> }));
vi.mock('../lib/session', () => ({
  clearSession: vi.fn(),
  getUser: () => ({ role: 'HELPDESK', name: 'Helpdesk', flatNo: null }),
}));

function NavigationButton() {
  const navigate = useNavigate();
  return <button onClick={() => navigate('/other')}>Navigate</button>;
}

function renderLayout() {
  const container = document.createElement('div');
  document.body.appendChild(container);
  const root = createRoot(container);
  act(() => {
    root.render(
      <MemoryRouter initialEntries={['/helpdesk']}>
        <Routes>
          <Route
            path="*"
            element={
              <StaffLayout>
                <NavigationButton />
              </StaffLayout>
            }
          />
        </Routes>
      </MemoryRouter>,
    );
  });
  return { container, root };
}

function cleanup(root: Root, container: HTMLDivElement) {
  act(() => root.unmount());
  container.remove();
}

describe('StaffLayout mobile menu', () => {
  let rendered: { container: HTMLDivElement; root: Root } | undefined;

  afterEach(() => {
    if (rendered) cleanup(rendered.root, rendered.container);
    rendered = undefined;
  });

  it('opens and closes from the button, close button, backdrop, Escape, and navigation', () => {
    rendered = renderLayout();
    const menuButton = rendered.container.querySelector(
      '[aria-controls="staff-menu"]',
    ) as HTMLButtonElement;
    const menu = rendered.container.querySelector('[role="dialog"]') as HTMLElement;
    expect(menu.classList.contains('hidden')).toBe(true);

    act(() => menuButton.click());
    expect(menu.classList.contains('hidden')).toBe(false);
    expect(menuButton.getAttribute('aria-expanded')).toBe('true');

    const closeButton = rendered.container.querySelector(
      '[aria-label="closeMenu"]',
    ) as HTMLButtonElement;
    act(() => closeButton.click());
    expect(menu.classList.contains('hidden')).toBe(true);

    act(() => menuButton.click());
    const backdrop = rendered.container.querySelector('button.fixed.inset-0') as HTMLButtonElement;
    act(() => backdrop.click());
    expect(menu.classList.contains('hidden')).toBe(true);

    act(() => menuButton.click());
    act(() => document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' })));
    expect(menu.classList.contains('hidden')).toBe(true);

    act(() => menuButton.click());
    const navigateButton = rendered.container.querySelector(
      'button:not([aria-controls]):not([aria-label])',
    ) as HTMLButtonElement;
    act(() => navigateButton.click());
    expect(menu.classList.contains('hidden')).toBe(true);
  });
});
