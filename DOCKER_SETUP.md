# Docker Setup Documentation

## ✅ Complete Docker Configuration

All services are fully dockerized and configured:

### Services Overview

1. **PostgreSQL Database** (`postgres`)
2. **Spring Boot Backend** (`backend`)
3. **Angular Frontend** (`frontend`)

## 🗄️ Database Setup

### PostgreSQL Configuration

- **Image**: `postgres:16-alpine` (latest stable)
- **Database**: `banking_db`
- **User**: `banking_user`
- **Password**: `banking_pass`
- **Port**: `5432` (exposed to host)
- **Volume**: `postgres_data` (persistent storage)
- **Health Check**: Configured with `pg_isready`
- **Initialization**: SQL script at `backend/src/main/resources/db/init.sql`

### Database Connection

**In Docker:**
- Backend connects via: `jdbc:postgresql://postgres:5432/banking_db`
- Uses service name `postgres` for internal Docker network communication

**Local Development:**
- Connection: `jdbc:postgresql://localhost:5432/banking_db`
- Configured in `application-dev.yml`

### Schema Management

- **Auto-creation**: JPA/Hibernate with `ddl-auto: update`
- **Tables**: Created automatically from entity classes
- **Initial Data**: Admin user created via `DataInitializer.java`

## 🐳 Docker Compose Features

### Network Configuration
- **Custom Network**: `banking-network` (bridge driver)
- **Service Communication**: Services communicate via service names
- **Isolation**: All services on same network for secure communication

### Health Checks

**PostgreSQL:**
```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U banking_user -d banking_db"]
  interval: 10s
  timeout: 5s
  retries: 5
```

**Backend:**
```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

### Dependencies

- Backend waits for PostgreSQL to be healthy
- Frontend waits for Backend to be healthy
- Proper startup order guaranteed

### Restart Policies

All services configured with `restart: unless-stopped` for:
- Automatic recovery on failure
- Container restart on Docker daemon restart
- Production-ready resilience

## 📦 Service Details

### 1. PostgreSQL Service

```yaml
postgres:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: banking_db
    POSTGRES_USER: banking_user
    POSTGRES_PASSWORD: banking_pass
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./backend/src/main/resources/db/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
```

**Features:**
- ✅ Persistent data storage
- ✅ Database initialization script
- ✅ Health check configured
- ✅ UTF-8 encoding

### 2. Backend Service

```yaml
backend:
  build: ./backend
  environment:
    DB_USERNAME: banking_user
    DB_PASSWORD: banking_pass
    JWT_SECRET: [configured]
  depends_on:
    postgres:
      condition: service_healthy
```

**Features:**
- ✅ Multi-stage Docker build (Maven → JRE)
- ✅ Health check via Spring Boot Actuator
- ✅ Automatic database connection
- ✅ Environment variable configuration

### 3. Frontend Service

```yaml
frontend:
  build: ./frontend
  depends_on:
    backend:
      condition: service_healthy
```

**Features:**
- ✅ Multi-stage Docker build (Node → Nginx)
- ✅ Nginx reverse proxy to backend
- ✅ Production-ready static file serving
- ✅ SPA routing support

## 🚀 Running the Application

### Start All Services

```bash
docker-compose up --build
```

### Start in Detached Mode

```bash
docker-compose up -d --build
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f postgres
docker-compose logs -f frontend
```

### Stop Services

```bash
docker-compose down
```

### Stop and Remove Volumes

```bash
docker-compose down -v
```

## 🔍 Verification

### Check Service Status

```bash
docker-compose ps
```

### Check Database Connection

```bash
# From host
psql -h localhost -p 5432 -U banking_user -d banking_db

# From backend container
docker-compose exec backend sh
# Then test connection
```

### Check Health Endpoints

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:4200
```

## 📊 Database Access

### From Host Machine

```bash
psql -h localhost -p 5432 -U banking_user -d banking_db
# Password: banking_pass
```

### From Docker Container

```bash
docker-compose exec postgres psql -U banking_user -d banking_db
```

### Using Docker Exec

```bash
docker exec -it banking-postgres psql -U banking_user -d banking_db
```

## 🔧 Troubleshooting

### Database Connection Issues

1. **Check PostgreSQL is running:**
   ```bash
   docker-compose ps postgres
   ```

2. **Check logs:**
   ```bash
   docker-compose logs postgres
   ```

3. **Verify network:**
   ```bash
   docker network inspect banking-network
   ```

### Backend Won't Start

1. **Check database is ready:**
   ```bash
   docker-compose exec postgres pg_isready -U banking_user
   ```

2. **Check backend logs:**
   ```bash
   docker-compose logs backend
   ```

3. **Verify environment variables:**
   ```bash
   docker-compose exec backend env | grep DB
   ```

### Frontend Issues

1. **Check backend is accessible:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Check nginx configuration:**
   ```bash
   docker-compose exec frontend cat /etc/nginx/conf.d/default.conf
   ```

## 🔐 Security Notes

### Production Considerations

1. **Change Default Passwords:**
   - Update `POSTGRES_PASSWORD` in `docker-compose.yml`
   - Update `JWT_SECRET` with strong random key

2. **Environment Variables:**
   - Use `.env` file for sensitive data
   - Never commit secrets to repository

3. **Network Security:**
   - Consider removing port exposures in production
   - Use reverse proxy for external access

4. **Database:**
   - Use strong passwords
   - Limit database user permissions
   - Enable SSL connections in production

## 📝 Environment Variables

### Backend Environment Variables

- `DB_USERNAME`: Database username (default: banking_user)
- `DB_PASSWORD`: Database password (default: banking_pass)
- `JWT_SECRET`: JWT signing secret (change in production!)
- `SPRING_DATASOURCE_URL`: Database connection URL

### Frontend Environment Variables

- `API_URL`: Backend API URL (default: http://backend:8080)

## ✅ Summary

**Database:**
- ✅ PostgreSQL 16 configured
- ✅ Persistent volume
- ✅ Health checks
- ✅ Initialization script
- ✅ Auto-schema creation via JPA

**Docker:**
- ✅ All services containerized
- ✅ Health checks configured
- ✅ Network isolation
- ✅ Restart policies
- ✅ Dependency management
- ✅ Volume persistence

**Everything is ready to run with:**
```bash
docker-compose up --build
```

