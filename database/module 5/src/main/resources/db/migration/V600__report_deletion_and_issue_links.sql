ALTER TABLE complaints ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE complaints ADD COLUMN issue_root_id BIGINT NULL;
ALTER TABLE complaints ADD CONSTRAINT fk_issue_root FOREIGN KEY(issue_root_id) REFERENCES complaints(id);
CREATE INDEX ix_visible_reports ON complaints(deleted_at,status,created_at);
CREATE INDEX ix_issue_reports ON complaints(issue_root_id,deleted_at,reporter_id);
