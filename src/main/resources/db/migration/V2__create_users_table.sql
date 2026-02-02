-- ============================================================================
-- V2: User System - Bảng users và user_identities
-- ============================================================================

CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID user',
    full_name  VARCHAR(100) NOT NULL COMMENT 'Họ và tên đầy đủ',
    status     INT NOT NULL DEFAULT 1 COMMENT '1=ACTIVE, 0=SUSPENDED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Thông tin người dùng';

-- ----------------------------------------------------------------------------

CREATE TABLE user_identities (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID tự tăng',
    user_id          BIGINT NOT NULL COMMENT 'ID user liên kết',
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
