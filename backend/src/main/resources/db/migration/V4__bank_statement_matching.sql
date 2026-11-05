ALTER TABLE bank_row DROP CONSTRAINT bank_row_status_check;
ALTER TABLE bank_row ADD CONSTRAINT bank_row_status_check
    CHECK (status IN ('MATCHED','QUEUED','DUPLICATE','RESOLVED','REJECTED','ERROR'));

ALTER TABLE bank_row ADD COLUMN matched_bill_id BIGINT REFERENCES bill(id);
ALTER TABLE bank_row ADD CONSTRAINT uq_bank_row_natural_key UNIQUE (reference, amount_paise, txn_date);
ALTER TABLE payment ADD CONSTRAINT uq_payment_bank_row UNIQUE (bank_row_id);

CREATE TABLE bank_row_audit (
  id BIGSERIAL PRIMARY KEY,
  bank_row_id BIGINT NOT NULL REFERENCES bank_row(id),
  actor_id BIGINT NOT NULL REFERENCES app_user(id),
  action VARCHAR(16) NOT NULL CHECK (action IN ('RESOLVE','REJECT')),
  reason VARCHAR(255),
  at TIMESTAMPTZ NOT NULL DEFAULT now()
);
