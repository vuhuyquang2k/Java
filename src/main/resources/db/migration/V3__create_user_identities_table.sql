-- V3: Create user_identities table
-- Bảng liên kết user với các nhà cung cấp OAuth (Google, Facebook, etc.)

CREATE TABLE user_identities (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID tự tăng, khóa chính',
    user_id INT NOT NULL COMMENT 'FK tới bảng users',
    provider INT NOT NULL COMMENT 'Nhà cung cấp OAuth: 1=GOOGLE',
    provider_user_id VARCHAR(255) NOT NULL COMMENT 'ID người dùng từ nhà cung cấp OAuth',
    email VARCHAR(255) COMMENT 'Email từ nhà cung cấp OAuth',
    email_status INT COMMENT 'Trạng thái email: 1=VERIFIED, 0=UNVERIFIED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian liên kết',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật cuối',
    
    CONSTRAINT fk_user_identity_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_one_identity_per_user UNIQUE (user_id),
    CONSTRAINT uq_provider_uid UNIQUE (provider, provider_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT = 'Bảng liên kết user với các nhà cung cấp OAuth';
