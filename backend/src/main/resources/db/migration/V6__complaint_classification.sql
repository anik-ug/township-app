ALTER TABLE complaint ADD COLUMN reopen_count INT NOT NULL DEFAULT 0;
ALTER TABLE complaint ADD COLUMN rating SMALLINT CHECK (rating BETWEEN 1 AND 5);
ALTER TABLE complaint ADD COLUMN first_response_at TIMESTAMPTZ;
ALTER TABLE complaint DROP CONSTRAINT IF EXISTS complaint_category_source_check;
ALTER TABLE complaint ADD CONSTRAINT complaint_category_source_check
    CHECK (category_source IN ('AI','RULE','KEYWORD','HUMAN'));
