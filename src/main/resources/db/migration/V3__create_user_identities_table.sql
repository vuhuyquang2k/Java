-- V3: Create user_identities table
CREATE TABLE user_identities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    provider INT NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    email_status INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_identity_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_one_identity_per_user UNIQUE (user_id),
    CONSTRAINT uq_provider_uid UNIQUE (provider, provider_user_id)
);
