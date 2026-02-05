-- ============================================================================
-- V4: E-Wallet System - Hệ thống ví điện tử cho shop bán tài khoản
-- ============================================================================

CREATE TABLE wallets (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID ví',
    user_id         BIGINT NOT NULL COMMENT 'ID user sở hữu ví',
    balance         DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Tổng số dư (VND)',
    pending_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Số dư đang giữ (withdrawal pending)',
    status          INT NOT NULL DEFAULT 1 COMMENT '1=ACTIVE, 2=FROZEN, 3=CLOSED',
    version         INT NOT NULL DEFAULT 1 COMMENT 'Optimistic lock version',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm tạo ví',
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời điểm cập nhật',

    UNIQUE KEY uq_wallet_user (user_id),
    INDEX idx_wallets_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Ví điện tử - mỗi user 1 ví duy nhất';

-- ----------------------------------------------------------------------------

CREATE TABLE wallet_transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID giao dịch',
    wallet_id        BIGINT NOT NULL COMMENT 'ID ví thực hiện giao dịch',
    transaction_type INT NOT NULL COMMENT '1=DEPOSIT, 2=WITHDRAWAL, 3=PURCHASE, 4=REFUND, 5=HOLD, 6=RELEASE',
    direction        INT NOT NULL COMMENT '1=CREDIT (vào ví), 2=DEBIT (ra khỏi ví)',
    amount           DECIMAL(15,2) NOT NULL COMMENT 'Số tiền giao dịch (luôn dương)',
    balance_before   DECIMAL(15,2) NOT NULL COMMENT 'Số dư trước giao dịch',
    balance_after    DECIMAL(15,2) NOT NULL COMMENT 'Số dư sau giao dịch',
    pending_before   DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Pending balance trước GD',
    pending_after    DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Pending balance sau GD',
    reference_type   VARCHAR(50) COMMENT 'Loại tham chiếu: DEPOSIT_REQUEST, ORDER, WITHDRAWAL_REQUEST',
    reference_id     BIGINT COMMENT 'ID của record tham chiếu',
    description      VARCHAR(255) COMMENT 'Mô tả giao dịch',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm giao dịch',

    INDEX idx_tx_wallet (wallet_id),
    INDEX idx_tx_created (created_at),
    INDEX idx_tx_type (transaction_type),
    INDEX idx_tx_ref (reference_type, reference_id),
    INDEX idx_tx_wallet_created (wallet_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Log giao dịch ví - immutable audit trail';

-- ----------------------------------------------------------------------------

CREATE TABLE deposit_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID yêu cầu nạp tiền',
    user_id             BIGINT NOT NULL COMMENT 'ID user yêu cầu nạp',
    amount              DECIMAL(15,2) NOT NULL COMMENT 'Số tiền đã chuyển khoản',
    transaction_code VARCHAR(100) NOT NULL COMMENT 'Mã GD từ app ngân hàng/biên lai - user nhập',
    transfer_reference  VARCHAR(100) COMMENT 'Mã tham chiếu nội bộ - server tự sinh',
    status              INT NOT NULL DEFAULT 1 COMMENT '1=PENDING, 2=APPROVED, 3=REJECTED, 4=CANCELLED',
    ip_address          VARCHAR(45) COMMENT 'IP address của request (IPv4/IPv6)',
    user_agent          VARCHAR(500) COMMENT 'Browser/app user agent',
    admin_note          VARCHAR(500) COMMENT 'Ghi chú của admin khi duyệt/từ chối',
    reject_reason       VARCHAR(255) COMMENT 'Lý do từ chối - bắt buộc khi reject',
    processed_by        BIGINT COMMENT 'ID admin xử lý',
    processed_at        TIMESTAMP NULL COMMENT 'Thời điểm xử lý',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm tạo yêu cầu',
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời điểm cập nhật',

    -- Không unique transaction_code vì mã GD từ các ngân hàng khác nhau có thể trùng
    -- Check duplicate qua: user_id + transaction_code + status (không REJECTED/CANCELLED)
    INDEX idx_deposit_user (user_id),
    INDEX idx_deposit_status (status),
    INDEX idx_deposit_status_created (status, created_at),
    INDEX idx_deposit_tx_code (transaction_code),
    INDEX idx_deposit_user_tx_code (user_id, transaction_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Yêu cầu nạp tiền - user chuyển khoản trước, khai báo sau';

-- ----------------------------------------------------------------------------

CREATE TABLE withdrawal_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID yêu cầu rút tiền',
    user_id             BIGINT NOT NULL COMMENT 'ID user yêu cầu rút',
    amount              DECIMAL(15,2) NOT NULL COMMENT 'Số tiền yêu cầu rút',
    fee                 DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Phí rút tiền (nếu có)',
    net_amount          DECIMAL(15,2) GENERATED ALWAYS AS (amount - fee) STORED COMMENT 'Số tiền thực nhận = amount - fee',
    bank_code           VARCHAR(20) COMMENT 'Mã ngân hàng: VCB, TCB, MB...',
    bank_name           VARCHAR(100) NOT NULL COMMENT 'Tên ngân hàng đầy đủ',
    account_number      VARCHAR(50) NOT NULL COMMENT 'Số tài khoản',
    account_holder      VARCHAR(100) NOT NULL COMMENT 'Tên chủ tài khoản',
    bank_transaction_id VARCHAR(100) COMMENT 'Mã GD từ ngân hàng - admin nhập khi hoàn thành',
    status              INT NOT NULL DEFAULT 1 COMMENT '1=PENDING, 2=PROCESSING, 3=COMPLETED, 4=FAILED, 5=REJECTED, 6=CANCELLED',
    ip_address          VARCHAR(45) COMMENT 'IP address của request',
    user_agent          VARCHAR(500) COMMENT 'Browser/app user agent',
    admin_note          VARCHAR(500) COMMENT 'Ghi chú của admin',
    reject_reason       VARCHAR(255) COMMENT 'Lý do từ chối/thất bại',
    processed_by        BIGINT COMMENT 'ID admin xử lý',
    processed_at        TIMESTAMP NULL COMMENT 'Thời điểm xử lý',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm tạo yêu cầu',
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời điểm cập nhật',

    INDEX idx_withdrawal_user (user_id),
    INDEX idx_withdrawal_status (status),
    INDEX idx_withdrawal_status_created (status, created_at),
    INDEX idx_withdrawal_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Yêu cầu rút tiền - hold tiền khi tạo, admin xử lý thủ công';
