import type { ButtonHTMLAttributes } from 'react';

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  loading?: boolean;
};

export function Button({
  children,
  variant = 'primary',
  loading = false,
  className = '',
  disabled,
  ...props
}: Props) {
  const color =
    variant === 'primary'
      ? 'bg-accent text-white hover:bg-accent-dark active:bg-accent-dark'
      : variant === 'danger'
        ? 'border border-danger text-danger hover:bg-red-50'
        : variant === 'ghost'
          ? 'text-accent hover:bg-slate-100'
          : 'border border-slate-300 bg-white hover:bg-slate-50 active:bg-slate-100';
  return (
    <button
      disabled={disabled || loading}
      className={`tap focus-ring rounded-lg px-4 py-2 font-semibold transition-colors duration-150 disabled:cursor-not-allowed disabled:opacity-50 ${color} ${className}`}
      {...props}
    >
      {loading ? (
        <span className="inline-flex items-center gap-2">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
          {children}
        </span>
      ) : (
        children
      )}
    </button>
  );
}
