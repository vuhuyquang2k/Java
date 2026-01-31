-- V1: Create roles table
-- Bảng lưu trữ các vai trò trong hệ thống

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID tự tăng, khóa chính',
    name VARCHAR(255) NOT NULL UNIQUE COMMENT 'Tên vai trò (unique)',
    description VARCHAR(255) COMMENT 'Mô tả chi tiết vai trò',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật cuối'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT = 'Bảng lưu trữ các vai trò trong hệ thống';
