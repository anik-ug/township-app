import type { T } from '../types';
import { Button } from './Button';

export function ErrorState({ error, retry, t }: { error: string; retry: () => void; t: T }) {
  return (
    <div className="py-8 text-center" role="alert">
      <p className="mb-4 text-danger">{error}</p>
      <Button variant="secondary" onClick={retry}>
        {t('retry')}
      </Button>
    </div>
  );
}
