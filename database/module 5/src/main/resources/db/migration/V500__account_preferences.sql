-- Optional private profile details and display preferences for every role.
CREATE TABLE account_profiles (
  user_id BIGINT PRIMARY KEY,
  date_of_birth DATE NULL,
  phone VARCHAR(24) NOT NULL DEFAULT '',
  address VARCHAR(250) NOT NULL DEFAULT '',
  city VARCHAR(80) NOT NULL DEFAULT '',
  postal_code VARCHAR(16) NOT NULL DEFAULT '',
  neighbourhood VARCHAR(100) NOT NULL DEFAULT '',
  language VARCHAR(5) NOT NULL DEFAULT 'en',
  theme VARCHAR(10) NOT NULL DEFAULT 'system',
  home_latitude DOUBLE NULL,
  home_longitude DOUBLE NULL,
  CONSTRAINT fk_account_profiles_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT ck_account_profiles_language CHECK (language IN ('en', 'hi')),
  CONSTRAINT ck_account_profiles_theme CHECK (theme IN ('light', 'dark', 'system'))
);
