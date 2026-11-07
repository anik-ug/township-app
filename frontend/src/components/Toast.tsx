export function Toast({ message }: { message: string }) {
  return message ? (
    <p aria-live="polite" className="text-danger">
      {message}
    </p>
  ) : null;
}
