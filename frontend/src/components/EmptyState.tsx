export function EmptyState({ text, action }: { text: string; action?: React.ReactNode }) {
  return (
    <div className="py-12 text-center text-muted">
      <svg
        className="mx-auto mb-3 h-8 w-8"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="1.5"
          d="M8 9h8M8 13h5m5 7-3-3H6a3 3 0 0 1-3-3V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v8a3 3 0 0 1-3 3Z"
        />
      </svg>
      <p>{text}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
