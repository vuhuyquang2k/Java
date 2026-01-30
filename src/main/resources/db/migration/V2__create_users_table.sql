-- V2: Create users table
-- Bảng lưu trữ thông tin người dùng

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID tự tăng, khóa chính',
    full_name VARCHAR(100) NOT NULL COMMENT 'Họ và tên đầy đủ',
    status INT NOT NULL DEFAULT 1 COMMENT 'Trạng thái: 1=ACTIVE, 0=SUSPENDED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo tài khoản',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật cuối'
) COMMENT = 'Bảng lưu trữ thông tin người dùng';
