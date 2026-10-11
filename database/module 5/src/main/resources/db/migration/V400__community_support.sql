-- One support per citizen per report.
CREATE TABLE complaint_supports (
  complaint_id BIGINT NOT NULL,
  citizen_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (complaint_id, citizen_id),
  CONSTRAINT fk_complaint_supports_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id),
  CONSTRAINT fk_complaint_supports_citizen FOREIGN KEY (citizen_id) REFERENCES users (id)
);
