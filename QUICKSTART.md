# Quick Start Guide

## 🚀 Running with Docker Compose (Recommended)

1. **Start all services:**
   ```bash
   docker-compose up --build
   ```

2. **Wait for services to be ready** (about 1-2 minutes)

3. **Access the application:**
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

## 👤 Default Credentials

**Admin User:**
- Username: `admin`
- Password: `admin123`

**Note:** You can register new users through the registration form.

## 📝 First Steps

1. **Login as admin** at http://localhost:4200
2. **Create a customer account:**
   - Register a new user
   - Login with the new user
   - Create a bank account
   - Make a transfer

3. **Explore admin features:**
   - View all accounts
   - Check audit logs
   - Monitor fraud events

## 🛠️ Troubleshooting

### Port Already in Use
If ports 4200, 8080, or 5432 are already in use:
- Stop the conflicting services
- Or modify ports in `docker-compose.yml`

### Database Connection Issues
- Wait for PostgreSQL to fully start (check health status)
- Verify environment variables in `docker-compose.yml`

### Frontend Not Loading
- Check browser console for errors
- Verify backend is running on port 8080
- Check CORS configuration

## 🔧 Development Mode

### Backend Only
```bash
cd backend
mvn spring-boot:run
```

### Frontend Only
```bash
cd frontend
npm install
ng serve
```

Make sure PostgreSQL is running on port 5432.

## 📚 Next Steps

- Read the full [README.md](README.md) for detailed documentation
- Explore the API via Swagger UI
- Review the codebase architecture
- Run tests: `cd backend && mvn test`

