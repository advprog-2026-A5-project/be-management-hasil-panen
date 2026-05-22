ALTER TABLE harvest_photos ADD COLUMN IF NOT EXISTS public_id VARCHAR(255);
ALTER TABLE harvest_photos ADD COLUMN IF NOT EXISTS original_filename VARCHAR(255);
ALTER TABLE harvest_photos ADD COLUMN IF NOT EXISTS content_type VARCHAR(100);
ALTER TABLE harvest_photos ADD COLUMN IF NOT EXISTS size_bytes BIGINT;

CREATE INDEX IF NOT EXISTS idx_harvest_reports_status ON harvest_reports(status);
CREATE INDEX IF NOT EXISTS idx_harvest_reports_harvest_date ON harvest_reports(harvest_date);
CREATE INDEX IF NOT EXISTS idx_harvest_reports_kebun_code ON harvest_reports(kebun_code);
CREATE INDEX IF NOT EXISTS idx_harvest_reports_mandor_id ON harvest_reports(mandor_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_created_at ON outbox_events(status, created_at);
