import { useEffect, useState } from 'react';
import i18n from '../i18n';

export function useLoad<T>(loader: () => Promise<T>) {
  const [state, setState] = useState<{ data?: T; error?: string; loading: boolean }>({
    loading: true,
  });
  const load = () => {
    setState({ loading: true });
    loader()
      .then((data) => setState({ data, loading: false }))
      .catch((error: unknown) =>
        setState({
          error: error instanceof Error ? error.message : i18n.t('requestFailed'),
          loading: false,
        }),
      );
  };
  useEffect(load, []);
  return { ...state, retry: load };
}
