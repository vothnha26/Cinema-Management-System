CREATE TABLE verification_codes (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  code varchar(10) NOT NULL,
  purpose varchar(50) NOT NULL,
  expires_at datetime(6) NOT NULL,
  used_at datetime(6) DEFAULT NULL,
  created_at datetime(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_verification_codes_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE verification_codes ADD INDEX idx_verification_user_purpose (user_id, purpose, used_at);
