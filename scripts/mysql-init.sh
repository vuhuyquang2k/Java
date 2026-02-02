#!/bin/bash
set -e

echo "Initializing Database..."

# Sử dụng biến môi trường từ docker-compose
# Export password để tránh warning trên CLI
export MYSQL_PWD="${MYSQL_ROOT_PASSWORD}"

# Thực hiện lệnh SQL sử dụng biến môi trường
mysql -u root <<-EOSQL
    CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\`;
    CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
    GRANT ALL PRIVILEGES ON \`${MYSQL_DATABASE}\`.* TO '${MYSQL_USER}'@'%';
    FLUSH PRIVILEGES;
EOSQL

echo "Database initialized successfully with user: ${MYSQL_USER}"
