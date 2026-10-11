CREATE TABLE categories (
 id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL UNIQUE, department_id BIGINT NOT NULL,
 CONSTRAINT fk_category_department FOREIGN KEY(department_id) REFERENCES departments(id)
);
INSERT INTO categories(id,name,department_id) SELECT 1,'Roads',id FROM departments WHERE name='Roads and Infrastructure';
INSERT INTO categories(id,name,department_id) SELECT 2,'Streetlights',id FROM departments WHERE name='Street Lighting';
INSERT INTO categories(id,name,department_id) SELECT 3,'Garbage',id FROM departments WHERE name='Sanitation';
INSERT INTO categories(id,name,department_id) SELECT 4,'Water supply',id FROM departments WHERE name='Water Services';
INSERT INTO categories(id,name,department_id) SELECT 5,'Drainage',id FROM departments WHERE name='Drainage';
CREATE TABLE complaints (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, reference VARCHAR(30) NOT NULL UNIQUE,
 reporter_id BIGINT NOT NULL, category_id BIGINT NOT NULL, department_id BIGINT NOT NULL,
 title VARCHAR(120) NOT NULL, description VARCHAR(2000) NOT NULL, address VARCHAR(250) NOT NULL,
 latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'REPORTED',
 assigned_officer_id BIGINT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, resolved_at TIMESTAMP NULL, closed_at TIMESTAMP NULL,
 CONSTRAINT fk_reporter FOREIGN KEY(reporter_id) REFERENCES users(id),
 CONSTRAINT fk_complaint_category FOREIGN KEY(category_id) REFERENCES categories(id),
 CONSTRAINT fk_complaint_department FOREIGN KEY(department_id) REFERENCES departments(id),
 CONSTRAINT fk_assigned_officer FOREIGN KEY(assigned_officer_id) REFERENCES users(id),
 CONSTRAINT ck_complaint_status CHECK(status IN ('REPORTED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','REJECTED')),
 CONSTRAINT ck_latitude CHECK(latitude BETWEEN -90 AND 90),
 CONSTRAINT ck_longitude CHECK(longitude BETWEEN -180 AND 180)
);
CREATE INDEX ix_complaint_reporter ON complaints(reporter_id,created_at);
CREATE INDEX ix_complaint_officer ON complaints(assigned_officer_id,status);
CREATE INDEX ix_complaint_status ON complaints(status,created_at);
CREATE INDEX ix_complaint_nearby ON complaints(category_id,latitude,longitude);
CREATE TABLE complaint_events (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, complaint_id BIGINT NOT NULL, actor_id BIGINT NOT NULL,
 action VARCHAR(40) NOT NULL, from_status VARCHAR(20) NULL, to_status VARCHAR(20) NOT NULL,
 note VARCHAR(2000) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_event_complaint FOREIGN KEY(complaint_id) REFERENCES complaints(id),
 CONSTRAINT fk_event_actor FOREIGN KEY(actor_id) REFERENCES users(id)
);
CREATE INDEX ix_event_complaint ON complaint_events(complaint_id,id);
CREATE TABLE attachments (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, complaint_id BIGINT NOT NULL, uploader_id BIGINT NOT NULL,
 purpose VARCHAR(20) NOT NULL, content_type VARCHAR(40) NOT NULL, image_data MEDIUMBLOB NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_attachment_complaint FOREIGN KEY(complaint_id) REFERENCES complaints(id),
 CONSTRAINT fk_attachment_user FOREIGN KEY(uploader_id) REFERENCES users(id),
 CONSTRAINT ck_attachment_purpose CHECK(purpose IN ('ORIGINAL','REPAIR'))
);
CREATE INDEX ix_attachment_complaint ON attachments(complaint_id,purpose,id);
