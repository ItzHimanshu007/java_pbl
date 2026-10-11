-- Departments route work; each category belongs to one department.
CREATE TABLE departments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_departments_name UNIQUE (name)
);

CREATE TABLE categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  department_id BIGINT NOT NULL,
  CONSTRAINT uq_categories_name UNIQUE (name),
  CONSTRAINT fk_categories_department FOREIGN KEY (department_id) REFERENCES departments (id)
);

CREATE TABLE complaints (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reference VARCHAR(40) NOT NULL,
  reporter_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  address VARCHAR(250) NOT NULL,
  latitude DOUBLE NOT NULL,
  longitude DOUBLE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'REPORTED',
  assigned_officer_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  closed_at TIMESTAMP NULL,
  CONSTRAINT uq_complaints_reference UNIQUE (reference),
  CONSTRAINT fk_complaints_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
  CONSTRAINT fk_complaints_category FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT fk_complaints_department FOREIGN KEY (department_id) REFERENCES departments (id),
  CONSTRAINT fk_complaints_officer FOREIGN KEY (assigned_officer_id) REFERENCES users (id),
  CONSTRAINT ck_complaints_status
    CHECK (status IN ('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'))
);

CREATE INDEX ix_complaints_reporter ON complaints (reporter_id, created_at);
CREATE INDEX ix_complaints_officer ON complaints (assigned_officer_id, status);
CREATE INDEX ix_complaints_status ON complaints (status, created_at);
CREATE INDEX ix_complaints_location ON complaints (category_id, latitude, longitude);

-- Append-only timeline. Notes are written by people, never by the database.
CREATE TABLE complaint_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id BIGINT NOT NULL,
  actor_id BIGINT NOT NULL,
  action VARCHAR(40) NOT NULL,
  from_status VARCHAR(20) NULL,
  to_status VARCHAR(20) NOT NULL,
  note VARCHAR(2000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_complaint_events_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id),
  CONSTRAINT fk_complaint_events_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX ix_complaint_events_complaint ON complaint_events (complaint_id, id);

-- Photographs are re-encoded as PNG before they are stored.
CREATE TABLE attachments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id BIGINT NOT NULL,
  uploader_id BIGINT NOT NULL,
  purpose VARCHAR(20) NOT NULL,
  content_type VARCHAR(40) NOT NULL,
  image_data LONGBLOB NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_attachments_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id),
  CONSTRAINT fk_attachments_uploader FOREIGN KEY (uploader_id) REFERENCES users (id),
  CONSTRAINT ck_attachments_purpose CHECK (purpose IN ('ORIGINAL', 'REPAIR'))
);

CREATE INDEX ix_attachments_complaint ON attachments (complaint_id, purpose);

INSERT INTO departments (name) VALUES
  ('Roads and Infrastructure'),
  ('Street Lighting'),
  ('Sanitation'),
  ('Water Services');

INSERT INTO categories (name, department_id) VALUES
  ('Roads', (SELECT id FROM departments WHERE name = 'Roads and Infrastructure')),
  ('Streetlights', (SELECT id FROM departments WHERE name = 'Street Lighting')),
  ('Garbage', (SELECT id FROM departments WHERE name = 'Sanitation')),
  ('Water supply', (SELECT id FROM departments WHERE name = 'Water Services')),
  ('Drainage', (SELECT id FROM departments WHERE name = 'Water Services'));
