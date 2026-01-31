-- ============================================================================
-- V3: User Identities - Liên kết user với các nhà cung cấp OAuth
-- ============================================================================

CREATE TABLE user_identities (
    id               INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID tự tăng',
    user_id          INT NOT NULL COMMENT 'ID user liên kết',
    provider         INT NOT NULL COMMENT '1=GOOGLE, 2=FACEBOOK, 3=GITHUB',
    provider_user_id VARCHAR(255) NOT NULL COMMENT 'ID người dùng từ OAuth provider',
    email            VARCHAR(255) COMMENT 'Email từ OAuth provider',
    email_status     INT COMMENT '1=VERIFIED, 0=UNVERIFIED',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian liên kết',
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',

    UNIQUE KEY uq_one_identity_per_user (user_id),
    UNIQUE KEY uq_provider_uid (provider, provider_user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Liên kết user với OAuth providers';
