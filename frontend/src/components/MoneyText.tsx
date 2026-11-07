import { formatMoney } from '../lib/format';

export function MoneyText({ paise, large = false }: { paise: number; large?: boolean }) {
  return (
    <span className={`nums ${large ? 'text-4xl font-bold tracking-tight' : 'font-medium'}`}>
      {formatMoney(paise)}
    </span>
  );
}
