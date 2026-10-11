CREATE TABLE account_profiles (
 user_id BIGINT PRIMARY KEY,
 date_of_birth DATE NULL, phone VARCHAR(24) NOT NULL DEFAULT '',
 address VARCHAR(250) NOT NULL DEFAULT '', city VARCHAR(80) NOT NULL DEFAULT '',
 postal_code VARCHAR(16) NOT NULL DEFAULT '', neighbourhood VARCHAR(100) NOT NULL DEFAULT '',
 language VARCHAR(2) NOT NULL DEFAULT 'en', theme VARCHAR(8) NOT NULL DEFAULT 'system',
 home_latitude DOUBLE NULL, home_longitude DOUBLE NULL,
 CONSTRAINT fk_profile_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT ck_profile_language CHECK(language IN ('en','hi')),
 CONSTRAINT ck_profile_theme CHECK(theme IN ('light','dark','system')),
 CONSTRAINT ck_home_location CHECK((home_latitude IS NULL AND home_longitude IS NULL) OR
   (home_latitude BETWEEN -90 AND 90 AND home_longitude BETWEEN -180 AND 180))
);
