CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name VARCHAR(80) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    department_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT ck_user_role CHECK (role IN ('CITIZEN','OFFICER','ADMIN'))
);
CREATE TABLE login_sessions (
    token_id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX ix_login_user ON login_sessions(user_id);
CREATE INDEX ix_login_expiry ON login_sessions(expires_at);
INSERT INTO departments(name) VALUES ('Roads and Infrastructure'), ('Street Lighting'), ('Sanitation'), ('Water Services'), ('Drainage');
