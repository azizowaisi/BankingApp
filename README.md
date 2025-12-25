# Secure Digital Banking & Payments Platform

A production-grade digital banking system with enterprise-level security, transactional integrity, auditability, clean architecture, and scalability. Built with Java Spring Boot backend and Angular frontend.

## 🏗️ Architecture

### System Overview

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Angular   │────────▶│ Spring Boot │────────▶│ PostgreSQL  │
│  Frontend   │         │   Backend   │         │  Database   │
└─────────────┘         └─────────────┘         └─────────────┘
     Port 4200            Port 8080              Port 5432
```

### Backend Architecture (Clean Layered)

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
    ↓
Security, Fraud Detection, Audit Modules
```

### Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.3.5
- Spring Security (JWT Authentication)
- Spring Data JPA (Hibernate)
- PostgreSQL 16
- Maven 3.9
- OpenAPI/Swagger 2.6.0
- JJWT 0.12.5

**Frontend:**
- Angular 19.0.0
- TypeScript 5.7.2
- Angular Material 19.0.0
- RxJS 7.8.1
- SCSS

**Infrastructure:**
- Docker & Docker Compose
- Nginx (Frontend)

## 🔐 Security Model

### Authentication & Authorization

- **JWT-based Authentication**: Secure token-based authentication
- **Role-Based Access Control (RBAC)**: 
  - `CUSTOMER`: Can manage own accounts, transfer money, view own transactions
  - `ADMIN`: Full access including all accounts, audit logs, fraud events
- **Password Security**: BCrypt hashing with salt
- **Token Expiration**: 24-hour JWT token validity

### Transaction Security

- **ACID Compliance**: All transfers use `@Transactional` for atomicity
- **Balance Validation**: Prevents overdrafts
- **Account Status Checks**: Only ACTIVE accounts can send/receive
- **Fraud Detection**: 
  - Daily transfer limits (configurable, default: 10,000 SEK)
  - Rapid transfer detection (configurable threshold)
  - Suspicious activity logging

### Audit Logging

All critical actions are logged:
- User logins/logouts
- Money transfers
- Account creation/modification
- Admin actions
- Fraud events

## 📊 Database Schema

### Core Tables

1. **users**
   - `id` (UUID, PK)
   - `username` (unique)
   - `password` (hashed)
   - `email`
   - `firstName`, `lastName`
   - `role` (CUSTOMER, ADMIN)
   - `enabled`

2. **accounts**
   - `id` (UUID, PK)
   - `iban` (unique, indexed)
   - `balance` (DECIMAL 19,2)
   - `status` (ACTIVE, FROZEN, CLOSED)
   - `user_id` (FK to users)

3. **transactions**
   - `id` (UUID, PK)
   - `sender_account_id` (FK)
   - `receiver_account_id` (FK)
   - `amount` (DECIMAL 19,2)
   - `timestamp`
   - `status` (PENDING, COMPLETED, FAILED, REJECTED)
   - `description`

4. **audit_logs**
   - `id` (UUID, PK)
   - `action` (enum)
   - `user_id` (FK, nullable)
   - `timestamp`
   - `details`
   - `ip_address`

5. **fraud_events**
   - `id` (UUID, PK)
   - `user_id` (FK)
   - `type` (enum)
   - `timestamp`
   - `description`
   - `amount`
   - `severity` (LOW, MEDIUM, HIGH, CRITICAL)

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17 (for local development)
- Node.js 18+ (for local development)
- Maven 3.9+ (for local development)

### Quick Start with Docker

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd BankingApp
   ```

2. **Start all services**
   ```bash
   docker-compose up --build
   ```

3. **Access the application**
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - PostgreSQL: localhost:5432

### Local Development

#### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will run on http://localhost:8080

#### Frontend

```bash
cd frontend
npm install
ng serve
```

Frontend will run on http://localhost:4200

## 📝 API Documentation

### Authentication Endpoints

**POST** `/api/auth/register`
- Register a new user
- Request body: `{ username, password, email, firstName, lastName }`

**POST** `/api/auth/login`
- Login and receive JWT token
- Request body: `{ username, password }`
- Response: `{ token, username, role }`

### Account Endpoints

**GET** `/api/accounts`
- Get current user's accounts
- Requires: Authentication

**POST** `/api/accounts`
- Create a new account
- Request body: `{ initialBalance?, userId?, role? }`
- Requires: Authentication

**GET** `/api/accounts/{iban}`
- Get account details by IBAN
- Requires: Authentication (own account or ADMIN)

### Transfer Endpoints

**POST** `/api/transfers`
- Transfer money between accounts
- Request body: `{ fromIban, toIban, amount, description? }`
- Requires: Authentication
- Validations:
  - Sufficient balance
  - Account status (ACTIVE)
  - Daily limit check
  - Rapid transfer check

**GET** `/api/transfers/history/{accountId}`
- Get transaction history for an account
- Requires: Authentication (own account or ADMIN)

### Admin Endpoints

**GET** `/api/admin/accounts`
- Get all accounts
- Requires: ADMIN role

**GET** `/api/admin/transactions`
- Get all transactions
- Requires: ADMIN role

**GET** `/api/admin/audit-logs`
- Get all audit logs
- Requires: ADMIN role

**GET** `/api/admin/fraud-events`
- Get all fraud events
- Requires: ADMIN role

**PUT** `/api/accounts/{accountId}/status?status={status}`
- Update account status (ACTIVE, FROZEN, CLOSED)
- Requires: ADMIN role

## 🧪 Testing

### Backend Tests

```bash
cd backend
mvn test
```

Test coverage includes:
- Unit tests for services
- Transaction rollback tests
- Fraud detection tests
- Security tests

### Frontend Tests

```bash
cd frontend
ng test
```

## 🔧 Configuration

### Backend Configuration (`application.yml`)

```yaml
banking:
  fraud:
    daily-transfer-limit: 10000.00
    rapid-transfer-threshold: 5
    rapid-transfer-window-minutes: 60

spring:
  security:
    jwt:
      secret: your-256-bit-secret-key
      expiration: 86400000  # 24 hours
```

### Environment Variables

- `DB_USERNAME`: PostgreSQL username (default: banking_user)
- `DB_PASSWORD`: PostgreSQL password (default: banking_pass)
- `JWT_SECRET`: JWT signing secret (change in production!)

## 📋 API Usage Examples

### Register User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securepass123",
    "email": "john@banking.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securepass123"
  }'
```

### Create Account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "initialBalance": 1000.00
  }'
```

### Transfer Money

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "fromIban": "SE1234567890123456789012",
    "toIban": "SE9876543210987654321098",
    "amount": 100.00,
    "description": "Payment for services"
  }'
```

## 🏦 Business Logic

### Transaction Handling

1. **Validation Phase**
   - Check account status (must be ACTIVE)
   - Verify sufficient balance
   - Validate amount (must be positive)
   - Prevent self-transfers

2. **Fraud Detection Phase**
   - Check daily transfer limit
   - Detect rapid transfer patterns
   - Log suspicious activity

3. **Execution Phase**
   - Debit sender account
   - Credit receiver account
   - Create transaction record
   - Log audit event

4. **Rollback on Failure**
   - All operations are transactional
   - Automatic rollback on any error

### Fraud Detection Logic

- **Daily Limit**: Configurable limit per user per day
- **Rapid Transfers**: Detects multiple transfers within a time window
- **Severity Levels**: LOW, MEDIUM, HIGH, CRITICAL
- **Automatic Logging**: All fraud events are persisted

## 🎨 Frontend Features

### Customer Dashboard

- **Accounts View**: List all accounts with balances and status
- **Transfer Money**: Form to transfer between accounts
- **Transaction History**: View all transactions for accounts
- **Profile**: User information display

### Admin Dashboard

- **All Accounts**: View and manage all user accounts
- **Freeze/Unfreeze**: Control account status
- **Audit Logs**: View all system audit events
- **Fraud Events**: Monitor suspicious activities

## 🔍 Monitoring & Logging

- All API requests are logged
- Audit trail for all critical operations
- Fraud events are tracked and stored
- Error handling with proper HTTP status codes

## 📦 Deployment

### Production Considerations

1. **Change JWT Secret**: Use a strong, randomly generated secret
2. **Database Security**: Use strong passwords and restrict access
3. **HTTPS**: Enable SSL/TLS in production
4. **Environment Variables**: Use secrets management
5. **Monitoring**: Set up application monitoring
6. **Backup**: Regular database backups

### Docker Production Deployment

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 🤝 Contributing

For production deployment, ensure:
- Comprehensive security audit
- Penetration testing
- Performance testing
- Compliance with banking regulations
- Additional security measures

## 📄 License

Proprietary - All rights reserved

---

**Note**: This system implements enterprise-level banking software architecture. Additional security measures, compliance checks, and regulatory approvals are required for production deployment.

# BankingApp
