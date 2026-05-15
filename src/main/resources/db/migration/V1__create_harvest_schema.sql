CREATE TABLE harvest_reports (
    harvest_id UUID PRIMARY KEY,
    buruh_id UUID NOT NULL,
    buruh_name VARCHAR(255),
    mandor_id UUID,
    farm_id UUID,
    harvest_date DATE NOT NULL,
    kilogram DECIMAL(12, 2) NOT NULL,
    report_text CLOB NOT NULL,
    status VARCHAR(32) NOT NULL,
    rejection_reason CLOB,
    approved_by UUID,
    approved_at TIMESTAMP,
    rejected_by UUID,
    rejected_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_harvest_reports_buruh_date UNIQUE (buruh_id, harvest_date)
);

CREATE TABLE harvest_photos (
    harvest_photo_id UUID PRIMARY KEY,
    harvest_id UUID NOT NULL,
    photo_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_harvest_photos_harvest FOREIGN KEY (harvest_id) REFERENCES harvest_reports(harvest_id)
);

CREATE TABLE outbox_events (
    event_id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(128) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload CLOB NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP,
    retry_count INT NOT NULL DEFAULT 0
);
