-- Owners can move a report to Deleted reports and restore it; linked reports share a root issue.
ALTER TABLE complaints ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE complaints ADD COLUMN issue_root_id BIGINT NULL;
ALTER TABLE complaints ADD CONSTRAINT fk_complaints_issue_root FOREIGN KEY (issue_root_id) REFERENCES complaints (id);
CREATE INDEX ix_complaints_issue_root ON complaints (issue_root_id);
