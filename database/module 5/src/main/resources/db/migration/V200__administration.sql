-- Officers belong to one department; administrators audit account changes.
ALTER TABLE users ADD COLUMN department_id BIGINT NULL;
ALTER TABLE users ADD CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id);

CREATE TABLE administration_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  actor_id BIGINT NOT NULL,
  action VARCHAR(40) NOT NULL,
  subject_id BIGINT NOT NULL,
  note VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_administration_events_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);
