# Base Project - Spring Boot Template

Java Spring Boot project với OAuth2, Flyway, và HashiCorp Vault.

## 📋 Yêu cầu

- Java 24+
- Docker & Docker Compose
- (Optional) Vault CLI: `brew install vault`

## 🚀 Quick Start

### 1. Clone & Setup

```bash
git clone <repo-url>
cd Java

# Copy file env mẫu
cp .env.example .env
# Điền các giá trị thật vào .env
```

### 2. Start Infrastructure

```bash
docker-compose up -d
```

Chờ khoảng 30s để tất cả services khởi động.

### 3. Setup Vault Secrets

Vào Vault UI: http://localhost:8200
- **Token:** `dev-root-token`

Tạo secret tại **Secrets Engines → secret → Create secret**:
- **Path:** `base-project`
- **Data:**
  ```
  database.password = (để trống hoặc password thật)
  google.client-id = YOUR_GOOGLE_CLIENT_ID
  google.client-secret = YOUR_GOOGLE_CLIENT_SECRET
  ```

### 4. Run Application

```bash
./gradlew bootRun
```

App chạy tại: http://localhost:8080

## 🔗 Quick Links

| Service | URL |
|---------|-----|
| App | http://localhost:8080 |
| Vault UI | http://localhost:8200 |
| MySQL | localhost:3306 |
| Kafka | localhost:9092, 9093 |
| Redis | localhost:6379 |

## 🧪 Test OAuth

Vào: http://localhost:8080/oauth2/authorization/google

## 📁 Project Structure

```
├── src/main/java/com/base/demo/
│   ├── configs/          # Security, Kafka configs
│   ├── controllers/      # REST controllers
│   ├── entities/         # JPA entities
│   ├── repositories/     # Data repositories
│   └── services/         # Business logic
├── src/main/resources/
│   ├── db/migration/     # Flyway SQL scripts
│   └── application.properties
├── scripts/
│   └── init-vault.sh     # Vault initialization
└── docker-compose.yml
```

## 🛑 Stop Everything

```bash
# Stop app
Ctrl + C

# Stop Docker
docker-compose down
```
