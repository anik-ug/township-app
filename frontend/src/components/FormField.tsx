import type { ReactNode } from 'react';

export function FormField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="block space-y-2">
      <span className="font-medium">{label}</span>
      {children}
    </label>
  );
}
