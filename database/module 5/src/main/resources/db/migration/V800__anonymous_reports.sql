-- Anonymous reports. The complaint is attributed to a disabled system account and the
-- real reporter is kept only as a keyed one-way hash (HMAC-SHA256), so staff and anyone
-- reading the database cannot see who reported it. The reporter's own account can still
-- prove ownership by recomputing the hash with the server's secret key.
ALTER TABLE complaints ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE complaints ADD COLUMN reporter_hash VARCHAR(64) NULL;
CREATE INDEX ix_complaints_reporter_hash ON complaints (reporter_hash);

INSERT INTO users (display_name, email, password_hash, role, enabled)
VALUES ('Anonymous citizen', 'anonymous@civicpulse.invalid', '!disabled-system-account', 'CITIZEN', FALSE);
