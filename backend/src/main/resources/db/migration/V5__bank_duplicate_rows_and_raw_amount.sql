ALTER TABLE bank_row DROP CONSTRAINT uq_bank_row_natural_key;
CREATE UNIQUE INDEX uq_bank_row_natural_key_non_duplicate
    ON bank_row (reference, amount_paise, txn_date)
    WHERE status <> 'DUPLICATE';

ALTER TABLE bank_row ADD COLUMN raw_amount VARCHAR(64);
