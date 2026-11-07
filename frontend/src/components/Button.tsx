import type { ButtonHTMLAttributes } from 'react';

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'danger';
};

export function Button({ children, variant = 'primary', ...props }: Props) {
  const color =
    variant === 'primary'
      ? 'bg-accent text-white hover:bg-accent-dark'
      : variant === 'danger'
        ? 'border border-red-200 text-danger hover:bg-red-50'
        : 'border border-slate-300 bg-white hover:bg-slate-50';
  return (
    <button className={`tap focus-ring rounded-lg px-4 py-2 font-semibold ${color}`} {...props}>
      {children}
    </button>
  );
}
