/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    spacing: { 0: '0', 1: '4px', 2: '8px', 3: '12px', 4: '16px', 5: '20px', 6: '24px', 8: '32px', 10: '40px', 12: '48px', 16: '64px' },
    borderRadius: { sm: '4px', DEFAULT: '8px', md: '10px', lg: '12px', full: '9999px' },
    fontSize: { xs: '12px', sm: '14px', base: '16px', lg: '20px', xl: '24px', '2xl': '32px', '3xl': '36px' },
    extend: {
      spacing: { 11: '44px' },
      colors: {
        accent: '#0f766e', 'accent-dark': '#115e59', ink: '#1f2937', muted: '#64748b',
        danger: '#b91c1c', warning: '#b45309', success: '#047857', surface: '#f8fafc',
        amber: '#f59e0b', neutral: { 50: '#f8fafc', 100: '#f1f5f9', 200: '#e2e8f0', 300: '#cbd5e1', 400: '#94a3b8', 500: '#64748b', 600: '#475569', 700: '#334155', 800: '#1e293b', 900: '#0f172a' },
      },
      fontFamily: { sans: ['system-ui', "'Noto Sans Devanagari'", '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'sans-serif'] },
    },
  },
  plugins: [],
};
