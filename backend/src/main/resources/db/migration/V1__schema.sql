CREATE TABLE flat (
  id BIGSERIAL PRIMARY KEY,
  tower VARCHAR(8) NOT NULL,
  flat_no VARCHAR(16) NOT NULL UNIQUE,
  area_sqft INT NOT NULL CHECK (area_sqft > 0),
  upi_id VARCHAR(64)
);

CREATE TABLE app_user (
  id BIGSERIAL PRIMARY KEY,
  phone VARCHAR(15) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  role VARCHAR(16) NOT NULL CHECK (role IN ('RESIDENT','HELPDESK','ACCOUNTS','COMMITTEE')),
  flat_id BIGINT REFERENCES flat(id),
  is_tenant BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE late_fee_rule (
  id BIGSERIAL PRIMARY KEY,
  grace_days INT NOT NULL,
  annual_rate_bps INT NOT NULL,
  cap_paise BIGINT,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE bill (
  id BIGSERIAL PRIMARY KEY,
  flat_id BIGINT NOT NULL REFERENCES flat(id),
  bill_month DATE NOT NULL,
  amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
  due_date DATE NOT NULL,
  UNIQUE (flat_id, bill_month)
);

CREATE TABLE bank_upload (
  id BIGSERIAL PRIMARY KEY,
  file_name VARCHAR(200) NOT NULL,
  uploaded_by BIGINT NOT NULL REFERENCES app_user(id),
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE bank_row (
  id BIGSERIAL PRIMARY KEY,
  upload_id BIGINT NOT NULL REFERENCES bank_upload(id),
  txn_date DATE NOT NULL,
  amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
  reference VARCHAR(100) NOT NULL,
  upi_id VARCHAR(64),
  narration VARCHAR(255),
  status VARCHAR(16) NOT NULL CHECK (status IN ('MATCHED','QUEUED','DUPLICATE','RESOLVED')),
  confidence INT NOT NULL DEFAULT 0 CHECK (confidence BETWEEN 0 AND 100),
  explanation TEXT NOT NULL,
  matched_flat_id BIGINT REFERENCES flat(id),
  resolved_by BIGINT REFERENCES app_user(id)
);

CREATE TABLE payment (
  id BIGSERIAL PRIMARY KEY,
  bill_id BIGINT NOT NULL REFERENCES bill(id),
  amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
  method VARCHAR(16) NOT NULL CHECK (method IN ('TEST_ONLINE','BANK_MATCH','MANUAL')),
  reference VARCHAR(100),
  bank_row_id BIGINT REFERENCES bank_row(id),
  paid_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE complaint (
  id BIGSERIAL PRIMARY KEY,
  flat_id BIGINT NOT NULL REFERENCES flat(id),
  raised_by BIGINT NOT NULL REFERENCES app_user(id),
  description TEXT NOT NULL,
  category VARCHAR(24) NOT NULL,
  urgency VARCHAR(8) NOT NULL CHECK (urgency IN ('LOW','MEDIUM','HIGH','CRITICAL')),
  category_source VARCHAR(8) NOT NULL DEFAULT 'AI' CHECK (category_source IN ('AI','RULE','HUMAN')),
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED')),
  assignee_id BIGINT REFERENCES app_user(id),
  duplicate_of BIGINT REFERENCES complaint(id),
  escalated BOOLEAN NOT NULL DEFAULT FALSE,
  escalation_reason VARCHAR(100),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  closed_at TIMESTAMPTZ
);

CREATE TABLE complaint_event (
  id BIGSERIAL PRIMARY KEY,
  complaint_id BIGINT NOT NULL REFERENCES complaint(id),
  actor_id BIGINT REFERENCES app_user(id),
  from_status VARCHAR(16),
  to_status VARCHAR(16) NOT NULL,
  note VARCHAR(255),
  at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_bill_flat ON bill(flat_id);
CREATE INDEX idx_payment_bill ON payment(bill_id);
CREATE INDEX idx_complaint_status ON complaint(status);
CREATE INDEX idx_bank_row_status ON bank_row(status);
